-- ============================================================
-- FILE: insert_products.sql
-- MỤC ĐÍCH: Thêm sản phẩm mẫu (Điện thoại, Laptop, Phụ kiện, Thời trang)
-- CÁCH CHẠY: psql -U postgres -d ecommerce -f insert_products.sql
-- ============================================================

BEGIN;

-- ============================================================
-- BƯỚC 1: THÊM CATEGORIES (NẾU CHƯA CÓ)
-- ============================================================
INSERT INTO categories (name) VALUES 
    ('Electronics'),   -- Điện tử
    ('Laptops'),       -- Laptop
    ('Accessories'),   -- Phụ kiện
    ('Fashion')        -- Thời trang
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- BƯỚC 2: THÊM PRODUCTS
-- ============================================================
INSERT INTO products (name, price, thumbnail, description, category_id) VALUES
    -- Laptops
    ('MacBook Air M2 13 inch', 26500000, 'macbook-air-m2.jpg', 
     'Chip M2 siêu mạnh, màn hình Liquid Retina 13.6 inch, pin 18 giờ.', 
     (SELECT id FROM categories WHERE name = 'Laptops')),
     
    ('Dell XPS 13', 25900000, 'dell-xps-13.jpg', 
     'Intel Core i7, RAM 16GB, SSD 512GB, màn hình InfinityEdge.', 
     (SELECT id FROM categories WHERE name = 'Laptops')),
     
    ('Asus ROG Strix G15', 32000000, 'asus-rog-g15.jpg', 
     'Gaming laptop RTX 4060, màn hình 144Hz, tản nhiệt tốt.', 
     (SELECT id FROM categories WHERE name = 'Laptops')),
    
    -- Điện thoại
    ('Samsung Galaxy S23 Ultra', 23500000, 's23-ultra.jpg', 
     'Camera 200MP, bút S-Pen, chip Snapdragon 8 Gen 2.', 
     (SELECT id FROM categories WHERE name = 'Electronics')),
     
    ('iPhone 14 Pro', 27990000, 'iphone-14-pro.jpg', 
     'Dynamic Island, camera 48MP, chip A16 Bionic.', 
     (SELECT id FROM categories WHERE name = 'Electronics')),
    
    -- Phụ kiện
    ('Tai nghe AirPods Pro 2', 5990000, 'airpods-pro-2.jpg', 
     'Chống ồn chủ động, âm thanh không gian, sạc MagSafe.', 
     (SELECT id FROM categories WHERE name = 'Accessories')),
     
    ('Chuột Logitech MX Master 3S', 2490000, 'logitech-mx3s.jpg', 
     'Chuột không dây, cảm biến 8000 DPI, pin 70 ngày.', 
     (SELECT id FROM categories WHERE name = 'Accessories')),
     
    ('Bàn phím cơ Keychron K8', 2190000, 'keychron-k8.jpg', 
     'Hot-swap switch, RGB backlight, kết nối đa thiết bị.', 
     (SELECT id FROM categories WHERE name = 'Accessories')),
    
    -- Thời trang
    ('Áo Thun Cotton Basic', 250000, 'ao-thun-basic.jpg', 
     'Chất liệu 100% cotton co giãn 4 chiều, form regular.', 
     (SELECT id FROM categories WHERE name = 'Fashion')),
     
    ('Quần Jean Slim Fit', 450000, 'quan-jean-slim.jpg', 
     'Dáng ôm vừa vặn, vải denim cao cấp, phong cách trẻ trung.', 
     (SELECT id FROM categories WHERE name = 'Fashion'))
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- BƯỚC 3: THÊM PRODUCT VARIANTS
-- ============================================================

-- Variants cho MacBook Air M2
INSERT INTO product_variants (product_id, name, quantity, price) VALUES
    ((SELECT id FROM products WHERE name = 'MacBook Air M2 13 inch'), 'Màu Xám - 256GB', 15, 26500000),
    ((SELECT id FROM products WHERE name = 'MacBook Air M2 13 inch'), 'Màu Bạc - 256GB', 12, 26500000),
    ((SELECT id FROM products WHERE name = 'MacBook Air M2 13 inch'), 'Màu Xám - 512GB', 8, 31000000)
WHERE NOT EXISTS (
    SELECT 1 FROM product_variants pv
    JOIN products p ON pv.product_id = p.id
    WHERE p.name = 'MacBook Air M2 13 inch' AND pv.name = 'Màu Xám - 256GB'
);

-- Variants cho Dell XPS 13
INSERT INTO product_variants (product_id, name, quantity, price) VALUES
    ((SELECT id FROM products WHERE name = 'Dell XPS 13'), 'Bạc Platinum - 512GB', 10, 25900000),
    ((SELECT id FROM products WHERE name = 'Dell XPS 13'), 'Đen Carbon - 512GB', 7, 25900000)
WHERE NOT EXISTS (
    SELECT 1 FROM product_variants pv
    JOIN products p ON pv.product_id = p.id
    WHERE p.name = 'Dell XPS 13' AND pv.name = 'Bạc Platinum - 512GB'
);

-- Variants cho Samsung S23 Ultra
INSERT INTO product_variants (product_id, name, quantity, price) VALUES
    ((SELECT id FROM products WHERE name = 'Samsung Galaxy S23 Ultra'), 'Đen Phantom - 256GB', 20, 23500000),
    ((SELECT id FROM products WHERE name = 'Samsung Galaxy S23 Ultra'), 'Xanh Botanic - 256GB', 15, 23500000),
    ((SELECT id FROM products WHERE name = 'Samsung Galaxy S23 Ultra'), 'Đen Phantom - 512GB', 10, 26900000)
WHERE NOT EXISTS (
    SELECT 1 FROM product_variants pv
    JOIN products p ON pv.product_id = p.id
    WHERE p.name = 'Samsung Galaxy S23 Ultra' AND pv.name = 'Đen Phantom - 256GB'
);

-- Variants cho iPhone 14 Pro
INSERT INTO product_variants (product_id, name, quantity, price) VALUES
    ((SELECT id FROM products WHERE name = 'iPhone 14 Pro'), 'Tím Deep Purple - 256GB', 18, 27990000),
    ((SELECT id FROM products WHERE name = 'iPhone 14 Pro'), 'Đen Space Black - 256GB', 22, 27990000),
    ((SELECT id FROM products WHERE name = 'iPhone 14 Pro'), 'Tím Deep Purple - 512GB', 12, 32990000)
WHERE NOT EXISTS (
    SELECT 1 FROM product_variants pv
    JOIN products p ON pv.product_id = p.id
    WHERE p.name = 'iPhone 14 Pro' AND pv.name = 'Tím Deep Purple - 256GB'
);

-- Variants cho AirPods Pro 2
INSERT INTO product_variants (product_id, name, quantity, price) VALUES
    ((SELECT id FROM products WHERE name = 'Tai nghe AirPods Pro 2'), 'Trắng - Chính hãng VN/A', 50, 5990000)
WHERE NOT EXISTS (
    SELECT 1 FROM product_variants pv
    JOIN products p ON pv.product_id = p.id
    WHERE p.name = 'Tai nghe AirPods Pro 2' AND pv.name = 'Trắng - Chính hãng VN/A'
);

-- Variants cho Áo thun
INSERT INTO product_variants (product_id, name, quantity, price) VALUES
    ((SELECT id FROM products WHERE name = 'Áo Thun Cotton Basic'), 'Size M - Trắng', 100, 250000),
    ((SELECT id FROM products WHERE name = 'Áo Thun Cotton Basic'), 'Size L - Đen', 80, 250000),
    ((SELECT id FROM products WHERE name = 'Áo Thun Cotton Basic'), 'Size M - Xám', 90, 250000)
WHERE NOT EXISTS (
    SELECT 1 FROM product_variants pv
    JOIN products p ON pv.product_id = p.id
    WHERE p.name = 'Áo Thun Cotton Basic' AND pv.name = 'Size M - Trắng'
);

-- Variants cho Quần jean
INSERT INTO product_variants (product_id, name, quantity, price) VALUES
    ((SELECT id FROM products WHERE name = 'Quần Jean Slim Fit'), 'Size 30 - Xanh đậm', 45, 450000),
    ((SELECT id FROM products WHERE name = 'Quần Jean Slim Fit'), 'Size 32 - Xanh nhạt', 38, 450000),
    ((SELECT id FROM products WHERE name = 'Quần Jean Slim Fit'), 'Size 30 - Đen', 42, 450000)
WHERE NOT EXISTS (
    SELECT 1 FROM product_variants pv
    JOIN products p ON pv.product_id = p.id
    WHERE p.name = 'Quần Jean Slim Fit' AND pv.name = 'Size 30 - Xanh đậm'
);

COMMIT;

-- ============================================================
-- KIỂM TRA KẾT QUẢ
-- ============================================================
SELECT 
    c.name AS category,
    p.name AS product,
    p.price,
    COUNT(pv.id) AS variant_count,
    SUM(pv.quantity) AS total_stock
FROM products p
JOIN categories c ON p.category_id = c.id
LEFT JOIN product_variants pv ON pv.product_id = p.id
GROUP BY c.name, p.name, p.price
ORDER BY c.name, p.name;
