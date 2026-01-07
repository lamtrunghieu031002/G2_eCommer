-- ============================================================
-- FILE: add_keywords_column.sql
-- MỤC ĐÍCH: Thêm cột keywords vào bảng chatbot_faq
-- CÁCH CHẠY: psql -U postgres -d ecommerce -f add_keywords_column.sql
-- ============================================================

BEGIN;

-- 1. THÊM CỘT keywords (mảng TEXT)
ALTER TABLE chatbot_faq 
ADD COLUMN IF NOT EXISTS keywords TEXT[];

-- 2. CẬP NHẬT is_active nếu chưa có
ALTER TABLE chatbot_faq 
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- 3. UPDATE tất cả dòng hiện có
UPDATE chatbot_faq 
SET is_active = TRUE 
WHERE is_active IS NULL;

COMMIT;

-- Kiểm tra cấu trúc bảng
\d chatbot_faq
