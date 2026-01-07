BEGIN;

-- ============================================================
-- 1. CÁC BẢNG ECOMMERCE (GIỮ NGUYÊN CẤU TRÚC CŨ)
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    fullname VARCHAR(100),
    phone_number VARCHAR(10) NOT NULL UNIQUE,
    address VARCHAR(200),
    password VARCHAR(200) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    date_of_birth DATE,
    role_id BIGINT REFERENCES roles(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(350) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    thumbnail VARCHAR(300),
    description TEXT,
    category_id BIGINT REFERENCES categories(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(300),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BẢNG PRODUCT_VARIANTS GIỮ NGUYÊN CỘT: name, quantity
CREATE TABLE IF NOT EXISTS product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(255),
    price DECIMAL(10,2),
    quantity INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    fullname VARCHAR(100),
    email VARCHAR(255),
    phone_number VARCHAR(10),
    address VARCHAR(255),
    note VARCHAR(255),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'pending',
    total_money DECIMAL(10,2),
    shipping_method VARCHAR(100),
    shipping_address VARCHAR(255),
    shipping_date TIMESTAMP,
    tracking_number VARCHAR(255),
    payment_method VARCHAR(100),
    active BOOLEAN DEFAULT true,
    vnp_txn_ref VARCHAR(100), -- Tích hợp mã giao dịch VNPay
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_details (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    product_id BIGINT REFERENCES products(id),
    price DECIMAL(10,2) NOT NULL,
    number_of_products INT NOT NULL,
    total_money DECIMAL(10,2) NOT NULL,
    color VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    expiration_date TIMESTAMP,
    revoked BOOLEAN DEFAULT false,
    expired BOOLEAN DEFAULT false,
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL CHECK (discount_type IN ('PERCENT', 'FIXED')),
    discount_value DECIMAL(10,2) NOT NULL CHECK (discount_value > 0),
    minimum_order_amount DECIMAL(10,2),
    maximum_discount DECIMAL(10,2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    usage_limit INTEGER,
    used_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (end_date > start_date)
);

-- ============================================================
-- 2. CÁC BẢNG CHATBOT (BỔ SUNG)
-- ============================================================

CREATE TABLE IF NOT EXISTS chatbot_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    session_id VARCHAR(100) UNIQUE NOT NULL,
    started_at TIMESTAMP DEFAULT NOW(),
    ended_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chatbot_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(100) REFERENCES chatbot_sessions(session_id) ON DELETE CASCADE,
    role VARCHAR(10) NOT NULL CHECK (role IN ('USER', 'BOT')),
    message TEXT NOT NULL,
    intent VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chatbot_faq (
    id BIGSERIAL PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    keywords TEXT[], -- Lưu mảng từ khóa
    category VARCHAR(50),
    priority INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- 3. INSERT DỮ LIỆU MẪU (ĐÃ SỬA KHỚP TÊN CỘT CŨ)
-- ============================================================

-- Dữ liệu Roles
INSERT INTO roles (name) VALUES ('USER'), ('ADMIN') ON CONFLICT (name) DO NOTHING;

-- Dữ liệu Users
INSERT INTO users (fullname, phone_number, address, password, is_active, date_of_birth, role_id)
VALUES 
    ('Admin', '0123456789', 'Admin Address', '123456789', true, '1990-01-01', (SELECT id FROM roles WHERE name = 'ADMIN')),
    ('Test User', '0987654321', 'Test Address', '123456789', true, '1995-01-01', (SELECT id FROM roles WHERE name = 'USER'))
ON CONFLICT (phone_number) DO UPDATE SET password = EXCLUDED.password;

-- Dữ liệu Categories & Products
INSERT INTO categories (name) VALUES ('Electronics'), ('Clothing'), ('Books') ON CONFLICT (name) DO NOTHING;

INSERT INTO products (name, price, thumbnail, description, category_id) VALUES
    ('iPhone 15 Pro Max', 29990000, 'iphone15.jpg', 'Flagship mới nhất', (SELECT id FROM categories WHERE name = 'Electronics'))
ON CONFLICT DO NOTHING;

-- Dữ liệu Product Variants (SỬ DỤNG name, quantity THEO BẢNG CŨ)
INSERT INTO product_variants (product_id, name, quantity) VALUES
    (1, 'Titan Tự Nhiên - 256GB', 50),
    (1, 'Titan Đen - 256GB', 30)
ON CONFLICT DO NOTHING;

-- Dữ liệu Chatbot FAQ
INSERT INTO chatbot_faq (question, answer, keywords, category) VALUES
    ('Thanh toán như thế nào?', 'Bạn có thể thanh toán qua COD hoặc VNPay.', ARRAY['thanh toán', 'vnpay', 'cod'], 'payment');

-- Dữ liệu Chatbot FAQ
INSERT INTO chatbot_faq (question, answer, keywords, category, priority, is_active) VALUES
    ('Thanh toán như thế nào?', 'Bạn có thể thanh toán qua COD (tiền mặt khi nhận hàng) hoặc VNPay (chuyển khoản ngân hàng). Chúng tôi không thu phí thanh toán cho cả 2 hình thức.', ARRAY['thanh toán', 'vnpay', 'cod', 'payment', 'trả tiền'], 'payment', 10, TRUE),
    
    ('Chính sách giao hàng?', 'Chúng tôi giao hàng toàn quốc trong 2-5 ngày. Miễn phí ship cho đơn trên 500k. Đơn dưới 500k phí ship 30k. Bạn có thể theo dõi đơn hàng qua mã vận đơn.', ARRAY['giao hàng', 'ship', 'shipping', 'vận chuyển', 'delivery'], 'shipping', 10, TRUE),
    
    ('Chính sách bảo hành và đổi trả?', 'Sản phẩm được bảo hành 12 tháng. Đổi trả trong 7 ngày nếu lỗi nhà sản xuất. Sản phẩm phải còn nguyên seal, đầy đủ phụ kiện. Liên hệ hotline 1900xxxx để được hỗ trợ.', ARRAY['bảo hành', 'đổi trả', 'warranty', 'return', 'hoàn tiền'], 'warranty', 10, TRUE),
    
    ('Làm sao để mua hàng?', 'Bạn chọn sản phẩm → Thêm vào giỏ hàng → Thanh toán → Điền thông tin giao hàng. Hoặc chat với tôi để được tư vấn sản phẩm phù hợp.', ARRAY['mua hàng', 'đặt hàng', 'order', 'buy'], 'general', 5, TRUE),
    
    ('Shop có uy tín không?', 'Chúng tôi cam kết 100% hàng chính hãng, có tem chống hàng giả. Đã phục vụ hơn 10,000 khách hàng với 4.8/5 sao đánh giá.', ARRAY['uy tín', 'chính hãng', 'đánh giá', 'review'], 'general', 3, TRUE)
ON CONFLICT DO NOTHING;

-- 1. Xóa FK cũ
ALTER TABLE chatbot_messages
DROP CONSTRAINT IF EXISTS chatbot_messages_session_id_fkey;

-- 2. Thêm cột mới FK chuẩn
ALTER TABLE chatbot_messages
ADD COLUMN session_ref_id BIGINT REFERENCES chatbot_sessions(id);

-- 3. Migrate dữ liệu (ép kiểu rõ ràng)
UPDATE chatbot_messages cm
SET session_ref_id = cs.id
FROM chatbot_sessions cs
WHERE cm.session_id::TEXT = cs.session_id;

-- 4. Kiểm tra dữ liệu chưa migrate
SELECT * FROM chatbot_messages WHERE session_ref_id IS NULL;

-- 5. Xóa cột cũ
ALTER TABLE chatbot_messages DROP COLUMN session_id;

-- 6. Đổi tên cột
ALTER TABLE chatbot_messages
RENAME COLUMN session_ref_id TO session_id;


-- Thêm cột is_active vào DB
ALTER TABLE chatbot_faq 
  ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- Update data hiện có
UPDATE chatbot_faq SET is_active = TRUE WHERE is_active IS NULL;

-- Fix session_id FK
ALTER TABLE chatbot_messages DROP CONSTRAINT IF EXISTS chatbot_messages_session_id_fkey;
ALTER TABLE chatbot_messages ADD COLUMN session_ref_id BIGINT REFERENCES chatbot_sessions(id);
UPDATE chatbot_messages cm SET session_ref_id = cs.id FROM chatbot_sessions cs WHERE cm.session_id::TEXT = cs.session_id;
ALTER TABLE chatbot_messages DROP COLUMN session_id;
ALTER TABLE chatbot_messages RENAME COLUMN session_ref_id TO session_id;

-- Thêm is_active
ALTER TABLE chatbot_faq ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;
UPDATE chatbot_faq SET is_active = TRUE WHERE is_active IS NULL;
COMMIT;