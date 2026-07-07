# Chuyển dữ liệu từ database monolith sang các database mới

> Bỏ qua file này nếu bạn muốn bắt đầu với dữ liệu trống — các service tự tạo bảng khi chạy lần đầu (`ddl-auto: update`).

Giả sử database monolith cũ tên `ecommerce` chạy trên cùng PostgreSQL. Chạy các lệnh sau bằng `psql -U postgres`.

## 1. user_db (users, roles, tokens)

```bash
pg_dump -U postgres -d ecommerce -t roles -t users -t tokens --data-only --column-inserts > users_data.sql
psql -U postgres -d user_db -f users_data.sql
```
(Chạy user-service một lần trước để Hibernate tạo bảng, rồi mới import.)

## 2. product_db (categories, products, product_images, product_variants, reviews)

```bash
pg_dump -U postgres -d ecommerce -t categories -t products -t product_images -t product_variants --data-only --column-inserts > products_data.sql
psql -U postgres -d product_db -f products_data.sql
```

Bảng `reviews` có cột mới `user_name` (snapshot) — export kèm tên user từ DB cũ:

```sql
-- chay trong DB ecommerce (cu):
\copy (SELECT r.id, r.user_id, u.fullname AS user_name, r.product_id, r.rating, r.comment, r.created_at, r.updated_at
       FROM reviews r JOIN users u ON r.user_id = u.id)
  TO 'reviews.csv' CSV HEADER;

-- chay trong product_db (moi):
\copy reviews (id, user_id, user_name, product_id, rating, comment, created_at, updated_at)
  FROM 'reviews.csv' CSV HEADER;
SELECT setval(pg_get_serial_sequence('reviews','id'), (SELECT COALESCE(MAX(id),1) FROM reviews));
```

## 3. order_db (orders, order_details, coupons)

```bash
pg_dump -U postgres -d ecommerce -t orders -t coupons --data-only --column-inserts > orders_data.sql
psql -U postgres -d order_db -f orders_data.sql
```

Bảng `order_details` có các cột snapshot mới (`product_id`, `product_name`, `variant_name`, `thumbnail`) — export kèm join:

```sql
-- chay trong DB ecommerce (cu):
\copy (SELECT od.id, od.order_id, od.variant_id, pv.product_id,
              p.name AS product_name, pv.name AS variant_name, p.thumbnail,
              od.price, od.number_of_products, od.total_money
       FROM order_details od
       LEFT JOIN product_variants pv ON od.variant_id = pv.id
       LEFT JOIN products p ON pv.product_id = p.id)
  TO 'order_details.csv' CSV HEADER;

-- chay trong order_db (moi):
\copy order_details (id, order_id, variant_id, product_id, product_name, variant_name, thumbnail, price, number_of_products, total_money)
  FROM 'order_details.csv' CSV HEADER;
SELECT setval(pg_get_serial_sequence('order_details','id'), (SELECT COALESCE(MAX(id),1) FROM order_details));
```

## 4. chatbot_db (chatbot_faq, chatbot_sessions, chatbot_messages)

```bash
pg_dump -U postgres -d ecommerce -t chatbot_faq -t chatbot_sessions -t chatbot_messages --data-only --column-inserts > chatbot_data.sql
psql -U postgres -d chatbot_db -f chatbot_data.sql
```

Sau khi import mỗi DB, cập nhật sequence cho các bảng còn lại tương tự lệnh `setval` ở trên nếu gặp lỗi trùng id khi insert bản ghi mới.
