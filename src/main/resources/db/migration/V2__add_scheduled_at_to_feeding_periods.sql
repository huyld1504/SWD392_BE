-- ================================================================
-- Migration V2: Add scheduled_at to feeding_periods
-- Database: swd392_be (MySQL)
-- Date: 2026-03-07
-- ================================================================

ALTER TABLE feeding_periods
    ADD COLUMN scheduled_at DATETIME NULL
    AFTER executed_at;
