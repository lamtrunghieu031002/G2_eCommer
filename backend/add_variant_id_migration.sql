-- ============================================================
-- MIGRATION: THÊM VARIANT_ID VÀO ORDER_DETAILS
-- ============================================================

BEGIN;

-- 1. Thêm cột variant_id vào order_details
ALTER TABLE order_details
ADD COLUMN IF NOT EXISTS variant_id BIGINT;

-- 2. Thêm foreign key constraint
ALTER TABLE order_details
ADD CONSTRAINT fk_order_details_variant
FOREIGN KEY (variant_id) REFERENCES product_variants(id)
ON DELETE SET NULL;

-- 3. Tạo index để tăng tốc query
CREATE INDEX IF NOT EXISTS idx_order_details_variant_id ON order_details(variant_id);

-- 4. (Optional) Migrate dữ liệu cũ: Gán variant_id mặc định
-- Nếu có order_details cũ không có variant_id, gán variant đầu tiên của product
UPDATE order_details od
SET variant_id = pv.id
FROM (
    SELECT DISTINCT ON (product_id) id, product_id
    FROM product_variants
    ORDER BY product_id, id
) pv
WHERE od.product_id = pv.product_id
  AND od.variant_id IS NULL;

COMMIT;

-- Kiểm tra kết quả
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'order_details' AND column_name = 'variant_id';
