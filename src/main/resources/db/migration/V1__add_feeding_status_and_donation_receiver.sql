-- ================================================================
-- Migration V1: Feeding Reset & Donation Enhancement
-- Database: swd392_be (MySQL)
-- Date: 2026-03-07
-- ================================================================

-- ================================================================
-- 1. FEEDING_PERIODS: Thêm status, trigger_source, created_by
-- ================================================================

-- 1.1 Thêm cột status (trạng thái đợt feeding)
-- PENDING    = Admin tạo, chưa chạy
-- EXECUTING  = Đang xử lý (tránh chạy trùng)
-- COMPLETED  = Hoàn thành
-- FAILED     = Lỗi giữa chừng
ALTER TABLE feeding_periods
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED'
    AFTER executed_at;

-- 1.2 Thêm cột trigger_source (nguồn trigger)
-- AUTO_SCHEDULE = Scheduler tự động chạy
-- MANUAL_ADMIN  = Admin trigger thủ công
ALTER TABLE feeding_periods
    ADD COLUMN trigger_source VARCHAR(30) DEFAULT 'MANUAL_ADMIN'
    AFTER status;

-- 1.3 Thêm cột created_by (Admin nào tạo đợt feeding, NULL nếu auto)
ALTER TABLE feeding_periods
    ADD COLUMN created_by BIGINT NULL
    AFTER trigger_source;

ALTER TABLE feeding_periods
    ADD CONSTRAINT fk_feeding_period_created_by
    FOREIGN KEY (created_by) REFERENCES users(user_id)
    ON DELETE SET NULL;

-- 1.4 Thêm cột created_at (thời điểm tạo record, khác executed_at)
ALTER TABLE feeding_periods
    ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    AFTER created_by;

-- ================================================================
-- 2. ARTICLES: Thêm updated_at
-- ================================================================

ALTER TABLE articles
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    AFTER created_at;


-- ================================================================
-- 3. INDEX cho performance
-- ================================================================

-- Feeding: query theo status + tháng
CREATE INDEX idx_feeding_periods_status ON feeding_periods(status);
CREATE INDEX idx_feeding_periods_executed_at ON feeding_periods(executed_at);

-- Donation: thống kê theo sender, article, ngày
CREATE INDEX idx_donations_sender_id ON donations(sender_id);
CREATE INDEX idx_donations_article_id ON donations(article_id);
CREATE INDEX idx_donations_created_at ON donations(created_at);

-- Transaction: query theo wallet + ngày
CREATE INDEX idx_transactions_sender_wallet ON transactions(sender_wallet_id);
CREATE INDEX idx_transactions_receiver_wallet ON transactions(receiver_wallet_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
