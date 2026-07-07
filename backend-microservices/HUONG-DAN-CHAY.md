# Hướng dẫn chạy hệ thống (demo cho khách hàng)

Hệ thống gồm **6 microservice** (Spring Boot) + **PostgreSQL** (Docker) + **frontend Angular**.
Khách hàng chỉ cần mở trình duyệt vào **http://localhost:4200**.

```
Trình duyệt (4200)  ──►  API Gateway (8089)  ──►  6 service (8081..8085)  ──►  PostgreSQL (5433)
```

---

## A. Cài đặt cần có (chỉ 1 lần)

| Phần mềm | Kiểm tra |
|---|---|
| Java 21 | `java -version` |
| Maven | `mvn -version` |
| Docker Desktop | Mở sẵn, biểu tượng chạy |
| Node.js + npm | `node -v` (cho frontend) |

---

## B. Chuẩn bị lần đầu (làm 1 lần duy nhất)

### B1. Build backend
```powershell
cd D:\Ecom2\Ecom\backend-microservices
mvn clean package -DskipTests
```
Thành công sẽ thấy `BUILD SUCCESS`, mỗi service có file `.jar` trong thư mục `target`.

### B2. Cài thư viện frontend
```powershell
cd D:\Ecom2\Ecom\frontend
npm install
```

> **✅ Dữ liệu đã được nạp sẵn ngày 2026-07-08** (10 sản phẩm, 6 danh mục, 2 tài khoản, 5 đơn hàng cũ...). Bước B3 dưới đây chỉ cần làm lại nếu bạn xoá database hoặc muốn nạp lại từ đầu.

**Tài khoản demo (đăng nhập tại trang web):**

| Vai trò | Số điện thoại | Mật khẩu |
|---|---|---|
| Admin (quản trị) | `0123456789` | `123456789` |
| Khách hàng | `0987654321` | `123456789` |

### B3. Nạp dữ liệu sản phẩm (QUAN TRỌNG — nếu không có, trang web sẽ trống)

> DB gốc tên là **`ecom`** (không phải `ecommerce`), user `postgres`, mật khẩu `123456`, cổng 5432.

Các database mới đang **trống**. Dữ liệu gốc nằm ở database `ecommerce` cũ trên PostgreSQL local (cổng 5432) của bạn.

> Thay `<mat_khau>` bằng mật khẩu PostgreSQL local của bạn.

```powershell
# Chay MOT service len truoc de Hibernate tao bang (vd product-service), roi tat di.
# Sau do copy du lieu tu DB cu sang DB moi:

# 1. Categories + Products + Variants + Images  ->  product_db (cong 5433)
pg_dump -U postgres -h localhost -p 5432 -d ecommerce -t categories -t products -t product_images -t product_variants --data-only --column-inserts > products.sql
psql -U postgres -h localhost -p 5433 -d product_db -f products.sql   # pass: postgres

# 2. Users + Roles  ->  user_db
pg_dump -U postgres -h localhost -p 5432 -d ecommerce -t roles -t users --data-only --column-inserts > users.sql
psql -U postgres -h localhost -p 5433 -d user_db -f users.sql
```

Chi tiết đầy đủ (kể cả bảng `orders`, `reviews` có cột snapshot mới): xem [migrate-data.md](migrate-data.md).

### B4. Copy ảnh sản phẩm
```powershell
Copy-Item -Recurse ..\backend\uploads product-service\uploads
```
*(Nếu thư mục `..\backend` đã bị xoá thì bỏ qua — ảnh sẽ hiện ảnh mặc định.)*

---

## C. Chạy hàng ngày (mỗi lần demo)

### Cách 1 — Tự động (khuyên dùng)
```powershell
cd D:\Ecom2\Ecom\backend-microservices
.\start-all.ps1
```
Script sẽ: bật PostgreSQL → mở 6 cửa sổ cho 6 service. Đợi ~30-40 giây.

Rồi mở **terminal mới** chạy giao diện:
```powershell
cd D:\Ecom2\Ecom\frontend
npm start
```
Mở trình duyệt: **http://localhost:4200**

### Cách 2 — Thủ công (nếu script lỗi)
Mở postgres: `docker compose up -d postgres`
Rồi mở **6 terminal**, mỗi terminal 1 lệnh:
```powershell
cd user-service    ; java -jar target\user-service-0.0.1-SNAPSHOT.jar
cd product-service ; java -jar target\product-service-0.0.1-SNAPSHOT.jar
cd order-service   ; java -jar target\order-service-0.0.1-SNAPSHOT.jar
cd payment-service ; java -jar target\payment-service-0.0.1-SNAPSHOT.jar
cd chatbot-service ; java -jar target\chatbot-service-0.0.1-SNAPSHOT.jar
cd api-gateway     ; java -jar target\api-gateway-0.0.1-SNAPSHOT.jar
```

---

## D. Kiểm tra hệ thống đã lên

Mở trình duyệt hoặc PowerShell:
```powershell
# Phai tra ve "ok, service: user-service..."
curl http://localhost:8089/api/v1/healthcheck/health

# Phai tra ve danh sach san pham
curl "http://localhost:8089/api/v1/products?page=0&limit=5"
```
Nếu cả 2 lệnh có kết quả → sẵn sàng demo.

---

## E. Tắt hệ thống
```powershell
.\stop-all.ps1
```
Dữ liệu vẫn được giữ (nằm trong Docker volume), lần sau chạy lại là còn nguyên.

---

## F. Lưu ý về Kafka / Redis (tính năng đang phát triển)

Hai tính năng nâng cao (tự trừ kho khi đặt hàng qua Kafka, cache qua Redis) **chưa bật trong bản demo** vì:
- Image `bitnami/kafka:3.7` trong `docker-compose.yml` không còn tồn tại → cần đổi tag.
- Cổng 6379/9092 đang bị project khác (`movie-*`) chiếm.

**Không ảnh hưởng demo:** toàn bộ chức năng mua hàng, đăng nhập, chatbot vẫn chạy bình thường. Các service chỉ ghi vài dòng cảnh báo "không kết nối được Kafka" trong log — bỏ qua được. Khi nào hoàn thiện Kafka/Redis thì sửa image + đổi cổng rồi chạy `docker compose up -d` đầy đủ.

---

## G. Lỗi thường gặp

| Triệu chứng | Cách xử lý |
|---|---|
| Trang web trống, không có sản phẩm | Chưa nạp dữ liệu — làm lại **B3** |
| Đăng nhập xong bấm gì cũng "401/Unauthorized" | JWT_SECRET của các service không giống nhau — kiểm tra file `.env` trong từng service |
| Service báo `Connection refused ... 5433` | PostgreSQL chưa lên — chạy `docker compose up -d postgres`, đợi healthy |
| Ảnh sản phẩm không hiện | Chưa copy `uploads` (bước **B4**) |
| `port already in use` khi khởi động | Service cũ còn chạy — chạy `.\stop-all.ps1` trước |
