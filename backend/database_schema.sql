-- E-commerce Database Schema
-- Generated based on JPA entities

-- Enable UUID extension for PostgreSQL
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===========================================
-- TABLE CREATION
-- ===========================================

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    fullname VARCHAR(100),
    phone_number VARCHAR(10) NOT NULL UNIQUE,
    address VARCHAR(200),
    password VARCHAR(200) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    date_of_birth DATE,
    role_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(350) NOT NULL,
    price FLOAT,
    thumbnail VARCHAR(300),
    description TEXT,
    category_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Product Variants table
CREATE TABLE IF NOT EXISTS product_variants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    quantity INTEGER NOT NULL DEFAULT 0,
    product_id BIGINT
);

-- Product Images table
CREATE TABLE IF NOT EXISTS product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    image_url VARCHAR(300)
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    fullname VARCHAR(100),
    email VARCHAR(100),
    phone_number VARCHAR(100) NOT NULL,
    note VARCHAR(100),
    order_date DATE,
    status VARCHAR(50),
    total_money FLOAT,
    shipping_method VARCHAR(100),
    shipping_address TEXT,
    shipping_date DATE,
    tracking_number VARCHAR(100),
    payment_method VARCHAR(100),
    active BOOLEAN,
    vnp_txn_ref VARCHAR(100)
);

-- Order Details table
CREATE TABLE IF NOT EXISTS order_details (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT,
    variant_id BIGINT,
    price FLOAT NOT NULL,
    number_of_products INTEGER NOT NULL,
    total_money FLOAT NOT NULL
);

-- Reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Coupons table
CREATE TABLE IF NOT EXISTS coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    minimum_order_amount DECIMAL(10,2),
    maximum_discount DECIMAL(10,2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    usage_limit INTEGER,
    used_count INTEGER DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Tokens table
CREATE TABLE IF NOT EXISTS tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255),
    token_type VARCHAR(50),
    expiration_date TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    expired BOOLEAN DEFAULT FALSE,
    user_id BIGINT
);

-- Chatbot FAQ table
CREATE TABLE IF NOT EXISTS chatbot_faq (
    id BIGSERIAL PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(50),
    priority INTEGER,
    is_active BOOLEAN DEFAULT TRUE
);

-- Chatbot Sessions table
CREATE TABLE IF NOT EXISTS chatbot_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Chatbot Messages table
CREATE TABLE IF NOT EXISTS chatbot_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    sender VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    product_ids VARCHAR(500)
);

-- ===========================================
-- FOREIGN KEY CONSTRAINTS
-- ===========================================

-- Users table foreign keys
ALTER TABLE users ADD CONSTRAINT fk_users_role_id
    FOREIGN KEY (role_id) REFERENCES roles(id);

-- Products table foreign keys
ALTER TABLE products ADD CONSTRAINT fk_products_category_id
    FOREIGN KEY (category_id) REFERENCES categories(id);

-- Product Variants table foreign keys
ALTER TABLE product_variants ADD CONSTRAINT fk_product_variants_product_id
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

-- Product Images table foreign keys
ALTER TABLE product_images ADD CONSTRAINT fk_product_images_product_id
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

-- Orders table foreign keys
ALTER TABLE orders ADD CONSTRAINT fk_orders_user_id
    FOREIGN KEY (user_id) REFERENCES users(id);

-- Order Details table foreign keys
ALTER TABLE order_details ADD CONSTRAINT fk_order_details_order_id
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;
ALTER TABLE order_details ADD CONSTRAINT fk_order_details_variant_id
    FOREIGN KEY (variant_id) REFERENCES product_variants(id);

-- Reviews table foreign keys
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_user_id
    FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_product_id
    FOREIGN KEY (product_id) REFERENCES products(id);

-- Tokens table foreign keys
ALTER TABLE tokens ADD CONSTRAINT fk_tokens_user_id
    FOREIGN KEY (user_id) REFERENCES users(id);

-- Chatbot Sessions table foreign keys
ALTER TABLE chatbot_sessions ADD CONSTRAINT fk_chatbot_sessions_user_id
    FOREIGN KEY (user_id) REFERENCES users(id);

-- Chatbot Messages table foreign keys
ALTER TABLE chatbot_messages ADD CONSTRAINT fk_chatbot_messages_session_id
    FOREIGN KEY (session_id) REFERENCES chatbot_sessions(id) ON DELETE CASCADE;

-- ===========================================
-- INDEXES FOR PERFORMANCE
-- ===========================================

-- Users indexes
CREATE INDEX IF NOT EXISTS idx_users_phone_number ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);

-- Products indexes
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- Product Variants indexes
CREATE INDEX IF NOT EXISTS idx_product_variants_product_id ON product_variants(product_id);

-- Product Images indexes
CREATE INDEX IF NOT EXISTS idx_product_images_product_id ON product_images(product_id);

-- Orders indexes
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_order_date ON orders(order_date);

-- Order Details indexes
CREATE INDEX IF NOT EXISTS idx_order_details_order_id ON order_details(order_id);
CREATE INDEX IF NOT EXISTS idx_order_details_variant_id ON order_details(variant_id);

-- Reviews indexes
CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews(rating);

-- Coupons indexes
CREATE INDEX IF NOT EXISTS idx_coupons_code ON coupons(code);
CREATE INDEX IF NOT EXISTS idx_coupons_active ON coupons(is_active);
CREATE INDEX IF NOT EXISTS idx_coupons_date_range ON coupons(start_date, end_date);

-- Tokens indexes
CREATE INDEX IF NOT EXISTS idx_tokens_user_id ON tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_tokens_token ON tokens(token);

-- Chatbot indexes
CREATE INDEX IF NOT EXISTS idx_chatbot_sessions_session_id ON chatbot_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_chatbot_sessions_user_id ON chatbot_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_chatbot_messages_session_id ON chatbot_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_chatbot_faq_category ON chatbot_faq(category);
CREATE INDEX IF NOT EXISTS idx_chatbot_faq_active ON chatbot_faq(is_active);

-- ===========================================
-- SAMPLE DATA INSERTION
-- ===========================================

-- Insert roles
INSERT INTO roles (name) VALUES ('ADMIN'), ('USER')
ON CONFLICT (name) DO NOTHING;

-- Insert categories
INSERT INTO categories (name) VALUES
    ('Điện thoại'),
    ('Laptop'),
    ('Tablet'),
    ('Tai nghe'),
    ('Đồng hồ'),
    ('Phụ kiện')
ON CONFLICT DO NOTHING;

-- Insert admin user (password should be hashed in production)
INSERT INTO users (fullname, phone_number, address, password, is_active, role_id, created_at, updated_at)
VALUES ('Administrator', '0123456789', 'Admin Address', '$2a$10$hashedpassword', true, 1, NOW(), NOW())
ON CONFLICT (phone_number) DO NOTHING;

-- Insert sample user (password should be hashed in production)
INSERT INTO users (fullname, phone_number, address, password, is_active, role_id, date_of_birth, created_at, updated_at)
VALUES ('Nguyễn Văn A', '0987654321', '123 Đường ABC, Quận 1, TP.HCM', '$2a$10$hashedpassword', true, 2, '1990-01-15', NOW(), NOW())
ON CONFLICT (phone_number) DO NOTHING;

-- Insert sample products
INSERT INTO products (name, price, thumbnail, description, category_id, created_at, updated_at) VALUES
    ('iPhone 15 Pro Max', 29990000, '/images/iphone15.jpg', 'Điện thoại flagship của Apple với camera tuyệt vời', 1, NOW(), NOW()),
    ('Samsung Galaxy S24 Ultra', 25990000, '/images/s24ultra.jpg', 'Điện thoại Android cao cấp với S Pen', 1, NOW(), NOW()),
    ('MacBook Pro M3', 49990000, '/images/macbook.jpg', 'Laptop chuyên nghiệp cho developer', 2, NOW(), NOW()),
    ('Dell XPS 13', 32990000, '/images/dellxps.jpg', 'Ultrabook cao cấp với màn hình 4K', 2, NOW(), NOW()),
    ('iPad Air 5', 18990000, '/images/ipadair.jpg', 'Tablet đa năng với chip M1', 3, NOW(), NOW()),
    ('AirPods Pro 2', 5990000, '/images/airpods.jpg', 'Tai nghe không dây với chống ồn chủ động', 4, NOW(), NOW()),
    ('Apple Watch Series 9', 12990000, '/images/watch9.jpg', 'Đồng hồ thông minh với sức khỏe và thể thao', 5, NOW(), NOW()),
    ('Samsung Galaxy Watch 6', 8990000, '/images/galaxywatch.jpg', 'Đồng hồ Android với nhiều tính năng', 5, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insert product variants for sample products
INSERT INTO product_variants (name, quantity, product_id) VALUES
    ('128GB', 50, 1),
    ('256GB', 30, 1),
    ('512GB', 20, 1),
    ('1TB', 10, 1),
    ('128GB', 40, 2),
    ('256GB', 35, 2),
    ('512GB', 25, 2),
    ('1TB', 15, 2),
    ('8GB RAM', 20, 3),
    ('16GB RAM', 15, 3),
    ('32GB RAM', 10, 3),
    ('8GB RAM', 25, 4),
    ('16GB RAM', 20, 4),
    ('32GB RAM', 15, 4),
    ('64GB', 30, 5),
    ('256GB', 25, 5),
    ('Trắng', 40, 6),
    ('Đen', 35, 6),
    ('41mm', 30, 7),
    ('45mm', 25, 7),
    ('40mm', 35, 8),
    ('44mm', 30, 8)
ON CONFLICT DO NOTHING;

-- Insert sample product images
INSERT INTO product_images (product_id, image_url) VALUES
    (1, '/images/iphone15-1.jpg'),
    (1, '/images/iphone15-2.jpg'),
    (2, '/images/s24ultra-1.jpg'),
    (2, '/images/s24ultra-2.jpg'),
    (3, '/images/macbook-1.jpg'),
    (4, '/images/dellxps-1.jpg'),
    (5, '/images/ipadair-1.jpg'),
    (6, '/images/airpods-1.jpg'),
    (7, '/images/watch9-1.jpg'),
    (8, '/images/galaxywatch-1.jpg')
ON CONFLICT DO NOTHING;

-- Insert sample coupons
INSERT INTO coupons (code, name, description, discount_type, discount_value, minimum_order_amount, maximum_discount, start_date, end_date, is_active, usage_limit, created_at, updated_at) VALUES
    ('WELCOME10', 'Chào mừng giảm 10%', 'Giảm 10% cho đơn hàng đầu tiên', 'PERCENTAGE', 10.00, 100000, 500000, NOW(), NOW() + INTERVAL '30 days', true, 100, NOW(), NOW()),
    ('SALE50K', 'Giảm 50K', 'Giảm 50.000đ cho đơn hàng từ 500K', 'FIXED', 50000.00, 500000, NULL, NOW(), NOW() + INTERVAL '7 days', true, 50, NOW(), NOW()),
    ('FLASH20', 'Flash Sale 20%', 'Giảm 20% tất cả sản phẩm', 'PERCENTAGE', 20.00, 200000, 1000000, NOW(), NOW() + INTERVAL '1 day', true, 200, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Insert sample chatbot FAQ
INSERT INTO chatbot_faq (question, answer, category, priority, is_active) VALUES
    ('Làm thế nào để đặt hàng?', 'Để đặt hàng, bạn cần: 1. Đăng nhập tài khoản, 2. Thêm sản phẩm vào giỏ hàng, 3. Kiểm tra thông tin giao hàng, 4. Chọn phương thức thanh toán và xác nhận đơn hàng.', 'order', 1, true),
    ('Các phương thức thanh toán nào được hỗ trợ?', 'Chúng tôi hỗ trợ thanh toán bằng: 1. Thẻ tín dụng/ghi nợ, 2. Ví điện tử (MoMo, ZaloPay), 3. Thanh toán khi nhận hàng (COD), 4. Chuyển khoản ngân hàng.', 'payment', 1, true),
    ('Thời gian giao hàng là bao lâu?', 'Thời gian giao hàng thường là 2-5 ngày làm việc tùy khu vực. Đối với khu vực nội thành Hà Nội và TP.HCM là 1-2 ngày.', 'shipping', 1, true),
    ('Chính sách đổi trả như thế nào?', 'Chúng tôi hỗ trợ đổi trả trong vòng 7 ngày với sản phẩm còn nguyên tem mác. Chi tiết vui lòng xem tại trang chính sách đổi trả.', 'return', 2, true),
    ('Làm thế nào để theo dõi đơn hàng?', 'Bạn có thể theo dõi đơn hàng bằng cách: 1. Đăng nhập tài khoản, 2. Vào phần "Đơn hàng của tôi", 3. Click vào mã đơn hàng để xem chi tiết.', 'order', 2, true),
    ('Tôi có thể hủy đơn hàng không?', 'Đơn hàng có thể hủy trong vòng 24h sau khi đặt. Sau thời gian này, vui lòng liên hệ bộ phận chăm sóc khách hàng để được hỗ trợ.', 'order', 3, true)
ON CONFLICT DO NOTHING;

-- ===========================================
-- UTILITY FUNCTIONS (OPTIONAL)
-- ===========================================

-- Function to get product statistics
CREATE OR REPLACE FUNCTION get_product_stats(start_date DATE DEFAULT NULL, end_date DATE DEFAULT NULL)
RETURNS TABLE (
    product_name VARCHAR,
    total_quantity BIGINT,
    total_revenue DECIMAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.name,
        SUM(od.number_of_products),
        SUM(od.price * od.number_of_products)
    FROM products p
    JOIN product_variants pv ON p.id = pv.product_id
    JOIN order_details od ON pv.id = od.variant_id
    JOIN orders o ON od.order_id = o.id
    WHERE o.status = 'delivered'
    AND (start_date IS NULL OR o.order_date >= start_date)
    AND (end_date IS NULL OR o.order_date <= end_date)
    GROUP BY p.name
    ORDER BY SUM(od.price * od.number_of_products) DESC;
END;
$$ LANGUAGE plpgsql;

-- Function to check coupon validity
CREATE OR REPLACE FUNCTION is_coupon_valid(coupon_code VARCHAR, order_amount DECIMAL)
RETURNS BOOLEAN AS $$
DECLARE
    coupon_record RECORD;
BEGIN
    SELECT * INTO coupon_record
    FROM coupons
    WHERE code = coupon_code AND is_active = true;

    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;

    -- Check date validity
    IF NOW() < coupon_record.start_date OR NOW() > coupon_record.end_date THEN
        RETURN FALSE;
    END IF;

    -- Check usage limit
    IF coupon_record.usage_limit IS NOT NULL AND coupon_record.used_count >= coupon_record.usage_limit THEN
        RETURN FALSE;
    END IF;

    -- Check minimum order amount
    IF coupon_record.minimum_order_amount IS NOT NULL AND order_amount < coupon_record.minimum_order_amount THEN
        RETURN FALSE;
    END IF;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- ===========================================
-- VIEWS FOR COMMON QUERIES
-- ===========================================

-- View for active products with variants
CREATE OR REPLACE VIEW active_products AS
SELECT
    p.id,
    p.name,
    p.price,
    p.thumbnail,
    p.description,
    c.name as category_name,
    COUNT(pv.id) as total_variants,
    SUM(pv.quantity) as total_stock,
    p.created_at,
    p.updated_at
FROM products p
LEFT JOIN categories c ON p.category_id = c.id
LEFT JOIN product_variants pv ON p.id = pv.product_id
GROUP BY p.id, p.name, p.price, p.thumbnail, p.description, c.name, p.created_at, p.updated_at;

-- View for order summary
CREATE OR REPLACE VIEW order_summary AS
SELECT
    o.id,
    o.order_date,
    o.status,
    o.total_money,
    o.payment_method,
    u.fullname as customer_name,
    u.phone_number,
    COUNT(od.id) as total_items
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
LEFT JOIN order_details od ON o.id = od.order_id
GROUP BY o.id, o.order_date, o.status, o.total_money, o.payment_method, u.fullname, u.phone_number;

-- ===========================================
-- TRIGGERS FOR AUTOMATIC TIMESTAMPS
-- ===========================================

-- Trigger for users table
CREATE OR REPLACE FUNCTION update_users_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_users_timestamp();

-- Trigger for products table
CREATE OR REPLACE FUNCTION update_products_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_products_timestamp();

-- Trigger for reviews table
CREATE OR REPLACE FUNCTION update_reviews_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_reviews_timestamp();

-- Trigger for coupons table
CREATE OR REPLACE FUNCTION update_coupons_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_coupons_updated_at
    BEFORE UPDATE ON coupons
    FOR EACH ROW
    EXECUTE FUNCTION update_coupons_timestamp();

-- Trigger for chatbot_sessions table
CREATE OR REPLACE FUNCTION update_chatbot_sessions_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_chatbot_sessions_updated_at
    BEFORE UPDATE ON chatbot_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_chatbot_sessions_timestamp();
