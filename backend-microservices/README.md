# E-commerce Microservices

Backend monolith (`../backend`) được tách thành 6 module:

```
                     Angular (http://localhost:4200)
                                  |
                        +---------v----------+
                        |    api-gateway     |  :8089  (giu port cu -> frontend KHONG phai sua)
                        +---------+----------+
      +----------------+---------+-----------+----------------+
      |                |                     |                |
+-----v-----+   +------v------+   +----------v---+   +--------v-------+   +---------------+
| user-     |   | product-    |   | order-       |   | payment-       |   | chatbot-      |
| service   |   | service     |   | service      |   | service        |   | service       |
| :8081     |   | :8082       |   | :8083        |   | :8084          |   | :8085         |
+-----+-----+   +------+------+   +------+-------+   +----------------+   +-------+-------+
      |                |                 |             (khong co DB)             |
   user_db        product_db          order_db                              chatbot_db
```

| Module | Port | Domain | Database |
|---|---|---|---|
| api-gateway | **8089** | Route + CORS | - |
| user-service | 8081 | User, Role, JWT login | user_db |
| product-service | 8082 | Product, Category, Variant, Image, Review | product_db |
| order-service | 8083 | Order, OrderDetail, Coupon, Revenue | order_db |
| payment-service | 8084 | VNPay | - |
| chatbot-service | 8085 | Gemini AI, FAQ, Session | chatbot_db |

## Các thay đổi kiến trúc so với monolith

1. **Database-per-service** — mỗi service một database riêng, không còn khóa ngoại chéo domain:
   - `orders.user_id` là cột thường (không FK sang `users`).
   - `order_details` lưu **snapshot** (`product_name`, `variant_name`, `thumbnail`, `price`) tại thời điểm mua — đây cũng là chuẩn e-commerce (đơn hàng không đổi khi sản phẩm đổi tên/giá).
   - `reviews` lưu `user_id` + `user_name` snapshot.
2. **JWT stateless** — user-service phát token; các service khác chỉ verify chữ ký bằng secret chung (`JWT_SECRET`), không truy vấn DB.
3. **Gọi nội bộ giữa các service** qua REST (`RestTemplate`), endpoint `/internal/**`:
   - order-service → product-service: `GET /internal/variants/{id}` (giá, tên, tồn kho khi tạo đơn).
   - order-service / product-service → user-service: `GET /internal/users/{id}`.
   - chatbot-service → product-service: `ProductCatalogClient` (tìm kiếm sản phẩm cho chatbot).
   - **Lưu ý:** `/internal/**` không được route qua gateway — chỉ dùng trong mạng nội bộ.
4. **Doanh thu theo sản phẩm** (`/api/v1/revenues/by-product`) chuyển về order-service, thống kê trên snapshot `product_name`.

## Chạy hệ thống

### Bước 1 — Hạ tầng (PostgreSQL)

```bash
cd backend-microservices
docker compose up -d
```

Tạo sẵn 4 database: `user_db`, `product_db`, `order_db`, `chatbot_db` (user/pass: `postgres`/`postgres`).

> Không dùng Docker? Tạo 4 database thủ công bằng `psql -U postgres -f init-dbs.sql`.

### Bước 2 — Cấu hình

Mỗi service đọc file `.env` trong thư mục của nó (xem mẫu `.env.example`):
- `JWT_SECRET` — **bắt buộc**, phải giống nhau ở user/product/order/payment-service (sinh bằng `openssl rand -base64 32`).
- `DB_USERNAME`, `DB_PASSWORD` — tài khoản PostgreSQL của bạn (service có DB).
- `GEMINI_API_KEY` — bắt buộc nếu dùng chatbot.
- `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET` — bắt buộc nếu dùng thanh toán.

File `.env` không được commit (`.gitignore`).

### Bước 3 — Chạy các service

Mở 6 terminal (hoặc chạy nền), **thứ tự khuyến nghị**: user → product → order/payment/chatbot → gateway.

```bash
cd user-service     && mvn spring-boot:run     # :8081
cd product-service  && mvn spring-boot:run     # :8082
cd order-service    && mvn spring-boot:run     # :8083
cd payment-service  && mvn spring-boot:run     # :8084
cd chatbot-service  && mvn spring-boot:run     # :8085
cd api-gateway      && mvn spring-boot:run     # :8089
```

Lần chạy đầu, Hibernate tự tạo bảng (`ddl-auto: update`). Cần seed bảng `roles` trong `user_db`:

```sql
INSERT INTO roles (id, name) VALUES (1, 'user'), (2, 'admin');
```

### Bước 4 — Frontend

Không phải sửa gì: gateway chạy đúng port `8089` mà frontend Angular đang gọi.

```bash
cd ../frontend && npm start
```

### Ảnh sản phẩm

product-service đọc ảnh từ thư mục `uploads/` (working directory). Copy ảnh cũ:

```bash
cp -r ../backend/uploads product-service/uploads
```

### Dữ liệu cũ

Xem [migrate-data.md](migrate-data.md) để chuyển dữ liệu từ database monolith `ecommerce` sang các DB mới (có xử lý các cột snapshot mới).

## Kiểm tra nhanh

```bash
curl http://localhost:8089/api/v1/healthcheck/health      # -> user-service qua gateway
curl http://localhost:8089/api/v1/products?page=0&limit=5 # -> product-service qua gateway
```

## Bước tiếp theo (chưa làm trong lần tách này)

- **Kafka**: phát event `order-created` từ order-service, product-service consume để trừ tồn kho (hiện tồn kho **chưa bị trừ** khi đặt hàng — giống hành vi monolith cũ).
- **Redis**: cache danh sách sản phẩm, giỏ hàng, rate limiting ở gateway.
- **Service discovery (Eureka)** + Config Server nếu muốn đủ bộ Spring Cloud.
- Chatbot đang gọi product-service qua REST cho từng truy vấn — có thể thêm cache/CQRS sau.
