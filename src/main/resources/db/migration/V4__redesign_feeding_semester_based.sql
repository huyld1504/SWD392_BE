-- ================================================================
-- Migration V4: Redesign Feeding System (Semester-Based)
-- Database: swd392_be (MySQL)
-- Date: 2026-03-24
--
-- CHANGES:
--   1. Create new `semesters` table
--   2. Redesign `feeding_periods` → link to semester, add stats
--   3. Modify `user_feedings` → add fed_at, remove snapshot, unique constraint
--   4. Modify `transactions` → add semester_id FK
--
-- Disable safe update mode for DELETE without WHERE
-- ================================================================
SET SQL_SAFE_UPDATES = 0;
--   5. Clean up old data & indexes
--
-- ⚠️ WARNING: This migration DROPS old feeding data.
--    Run on a clean DB or backup first!
-- ================================================================


-- ================================================================
-- STEP 1: Create `semesters` table
-- ================================================================

CREATE TABLE IF NOT EXISTS semesters (
    semester_id     INT AUTO_INCREMENT PRIMARY KEY,
    semester_code   VARCHAR(10)  NOT NULL UNIQUE,           -- "SP26", "SU26", "FA26"
    semester_name   VARCHAR(50)  NOT NULL,                  -- "Spring 2026"
    start_date      DATE         NOT NULL,                  -- 2026-01-01
    end_date        DATE         NOT NULL,                  -- 2026-04-30
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, COMPLETED
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_semesters_code (semester_code),
    INDEX idx_semesters_status (status),
    INDEX idx_semesters_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ================================================================
-- STEP 2: Clear old feeding data (order matters due to FK)
-- ================================================================

-- 2.1 Remove old user_feedings records
DELETE FROM user_feedings;

-- 2.2 Remove old FEEDING transactions
DELETE FROM transactions WHERE transaction_type = 'FEEDING';

-- 2.3 Remove old feeding_periods records
DELETE FROM feeding_periods;


-- ================================================================
-- STEP 3: Redesign `feeding_periods` table
-- ================================================================

-- 3.1 Drop old columns if they exist (safe drop)
DROP PROCEDURE IF EXISTS drop_column_if_exists;
DELIMITER //
CREATE PROCEDURE drop_column_if_exists(IN tbl VARCHAR(64), IN col VARCHAR(64))
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = tbl
          AND COLUMN_NAME  = col
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', tbl, '` DROP COLUMN `', col, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL drop_column_if_exists('feeding_periods', 'period_name');
CALL drop_column_if_exists('feeding_periods', 'executed_at');
CALL drop_column_if_exists('feeding_periods', 'scheduled_at');
CALL drop_column_if_exists('feeding_periods', 'trigger_source');
DROP PROCEDURE IF EXISTS drop_column_if_exists;

-- 3.2 Drop old indexes if they exist (safe drop)
DROP PROCEDURE IF EXISTS drop_index_if_exists;
DELIMITER //
CREATE PROCEDURE drop_index_if_exists(IN tbl VARCHAR(64), IN idx VARCHAR(64))
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = tbl
          AND INDEX_NAME   = idx
    ) THEN
        SET @sql = CONCAT('DROP INDEX `', idx, '` ON `', tbl, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL drop_index_if_exists('feeding_periods', 'idx_feeding_periods_status');
CALL drop_index_if_exists('feeding_periods', 'idx_feeding_periods_executed_at');
DROP PROCEDURE IF EXISTS drop_index_if_exists;

-- 3.3 Add semester FK
ALTER TABLE feeding_periods
    ADD COLUMN semester_id INT NOT NULL AFTER period_id;

ALTER TABLE feeding_periods
    ADD CONSTRAINT fk_feeding_period_semester
    FOREIGN KEY (semester_id) REFERENCES semesters(semester_id)
    ON DELETE RESTRICT;

-- 3.4 Add running stats columns
ALTER TABLE feeding_periods
    ADD COLUMN total_coins_fed DECIMAL(12,2) NOT NULL DEFAULT 0.00
    AFTER grant_amount;

ALTER TABLE feeding_periods
    ADD COLUMN total_users_fed INT NOT NULL DEFAULT 0
    AFTER total_coins_fed;

-- 3.5 Add updated_at column
ALTER TABLE feeding_periods
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    AFTER created_at;

-- 3.6 Modify status enum values (was: PENDING/EXECUTING/COMPLETED/FAILED → now: ACTIVE/COMPLETED/CANCELLED)
-- Since ddl-auto=update handles enum via @Enumerated(STRING), just update existing data
-- All old data is already deleted in Step 2, so this is safe
ALTER TABLE feeding_periods
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- 3.7 New indexes
CREATE INDEX idx_feeding_periods_semester ON feeding_periods(semester_id);
CREATE INDEX idx_feeding_periods_status_new ON feeding_periods(status);


-- ================================================================
-- STEP 4: Modify `user_feedings` table
-- ================================================================

-- 4.1 Drop old column if exists (safe drop)
DROP PROCEDURE IF EXISTS drop_column_if_exists;
DELIMITER //
CREATE PROCEDURE drop_column_if_exists(IN tbl VARCHAR(64), IN col VARCHAR(64))
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = tbl
          AND COLUMN_NAME  = col
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', tbl, '` DROP COLUMN `', col, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL drop_column_if_exists('user_feedings', 'snapshot_earned_balance');
DROP PROCEDURE IF EXISTS drop_column_if_exists;

-- 4.2 Add fed_at column
ALTER TABLE user_feedings
    ADD COLUMN fed_at DATETIME DEFAULT CURRENT_TIMESTAMP
    AFTER amount_received;

-- 4.3 Add unique constraint: 1 user chỉ feed 1 lần/kỳ
ALTER TABLE user_feedings
    ADD CONSTRAINT uk_period_user UNIQUE (period_id, user_id);

-- 4.4 Index for faster lookups
CREATE INDEX idx_user_feedings_period ON user_feedings(period_id);
CREATE INDEX idx_user_feedings_user ON user_feedings(user_id);


-- ================================================================
-- STEP 5: Modify `transactions` table - add semester_id FK
-- ================================================================

ALTER TABLE transactions
    ADD COLUMN semester_id INT NULL AFTER transaction_type;

ALTER TABLE transactions
    ADD CONSTRAINT fk_transaction_semester
    FOREIGN KEY (semester_id) REFERENCES semesters(semester_id)
    ON DELETE SET NULL;

CREATE INDEX idx_transactions_semester ON transactions(semester_id);


-- ================================================================
-- STEP 6: Update wallets - set default balance for new wallets to 0
-- (This is handled by code change, not DB migration.
--  Existing wallets keep their current balance.)
-- ================================================================
-- No SQL needed. WalletServiceImpl.createDefaultWallet() will be changed to balance=0.


-- ================================================================
-- SUMMARY OF CHANGES:
-- ================================================================
-- NEW TABLE:   semesters (semester_id, semester_code, semester_name, start_date, end_date, status)
--
-- MODIFIED:    feeding_periods
--   ADDED:     semester_id (FK), total_coins_fed, total_users_fed, updated_at
--   REMOVED:   period_name, executed_at, scheduled_at, trigger_source
--
-- MODIFIED:    user_feedings
--   ADDED:     fed_at, UNIQUE(period_id, user_id)
--   REMOVED:   snapshot_earned_balance
--
-- MODIFIED:    transactions
--   ADDED:     semester_id (FK, nullable)
--
-- DATA:        All old feeding_periods, user_feedings, and FEEDING transactions are DELETED
-- ================================================================

-- Re-enable safe update mode
SET SQL_SAFE_UPDATES = 1;
