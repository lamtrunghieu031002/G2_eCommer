-- ============================================================
-- FILE: insert_faq.sql
-- MỤC ĐÍCH: Xóa FAQ cũ và insert lại đầy đủ các câu hỏi thường gặp
-- CÁCH CHẠY: psql -U postgres -d ecommerce -f insert_faq.sql
-- ============================================================

BEGIN;

-- 1. XÓA TOÀN BỘ FAQ CŨ
DELETE FROM chatbot_faq;

-- 2. RESET AUTO INCREMENT
ALTER SEQUENCE chatbot_faq_id_seq RESTART WITH 1;

-- 3. INSERT FAQ ĐẦY ĐỦ VỚI KEYWORDS PHỦ RỘNG
-- ✅ SỬ DỤNG E'...' ĐỂ \n ĐƯỢC NHẬN DIỆN ĐÚNG
INSERT INTO chatbot_faq (question, answer, keywords, category, priority, is_active) VALUES

-- FAQ THANH TOÁN
('Thanh toán như thế nào?', 
 E'Bạn có thể thanh toán qua 2 hình thức:\n• COD (tiền mặt khi nhận hàng)\n• VNPay (chuyển khoản ngân hàng)\n\nChúng tôi không thu phí thanh toán cho cả 2 hình thức.', 
 ARRAY['thanh toán', 'payment', 'trả tiền', 'vnpay', 'cod', 'chuyển khoản', 'tiền mặt', 'pay', 'phương thức thanh toán', 'hình thức thanh toán'], 
 'payment', 10, TRUE),

-- FAQ GIAO HÀNG
('Chính sách giao hàng?', 
 E'Chúng tôi giao hàng toàn quốc trong 2-5 ngày làm việc.\n\n• Miễn phí ship cho đơn hàng trên 500.000đ\n• Phí ship 30.000đ cho đơn dưới 500.000đ\n\nBạn có thể theo dõi đơn hàng qua mã vận đơn được gửi trong email.', 
 ARRAY['giao hàng', 'ship', 'shipping', 'vận chuyển', 'delivery', 'đặt hàng', 'nhận hàng', 'chuyển phát', 'freeship', 'miễn phí vận chuyển'], 
 'shipping', 10, TRUE),

-- FAQ BẢO HÀNH & ĐỔI TRẢ
('Chính sách bảo hành và đổi trả?', 
 E'Chính sách bảo hành:\n• Bảo hành 12 tháng cho lỗi nhà sản xuất\n• Đổi trả trong 7 ngày nếu sản phẩm lỗi\n\nĐiều kiện đổi trả:\n• Sản phẩm còn nguyên seal, đầy đủ phụ kiện\n• Có hóa đơn mua hàng\n\nLiên hệ hotline 1900xxxx để được hỗ trợ.', 
 ARRAY['bảo hành', 'đổi trả', 'warranty', 'return', 'hoàn tiền', 'refund', 'đổi sản phẩm', 'trả hàng', 'lỗi', 'hỏng'], 
 'warranty', 10, TRUE),

-- FAQ MUA HÀNG
('Làm sao để mua hàng?', 
 E'Quy trình mua hàng rất đơn giản:\n\n1. Chọn sản phẩm bạn muốn mua\n2. Thêm vào giỏ hàng\n3. Nhấn "Thanh toán"\n4. Điền thông tin giao hàng\n5. Chọn phương thức thanh toán\n6. Xác nhận đơn hàng\n\nHoặc chat với tôi để được tư vấn sản phẩm phù hợp!', 
 ARRAY['mua hàng', 'đặt hàng', 'order', 'buy', 'cách mua', 'hướng dẫn mua', 'quy trình', 'làm sao', 'thế nào', 'mua như thế nào'], 
 'general', 10, TRUE),

-- FAQ UY TÍN
('Shop có uy tín không?', 
 E'Chúng tôi cam kết:\n• 100% hàng chính hãng\n• Có tem chống hàng giả\n• Đã phục vụ hơn 10,000 khách hàng\n• Đánh giá 4.8/5 sao từ khách hàng\n• Chính sách bảo hành rõ ràng', 
 ARRAY['uy tín', 'chính hãng', 'đánh giá', 'review', 'tin cậy', 'chất lượng', 'thật giả', 'fake', 'authentic'], 
 'general', 5, TRUE),

-- FAQ LIÊN HỆ
('Làm sao để liên hệ với shop?', 
 E'Bạn có thể liên hệ với chúng tôi qua:\n• Hotline: 1900xxxx (8:00 - 22:00 hàng ngày)\n• Email: support@shop.com\n• Chat trực tiếp tại đây\n• Facebook: /ShopName', 
 ARRAY['liên hệ', 'contact', 'hotline', 'số điện thoại', 'email', 'hỗ trợ', 'support', 'facebook', 'zalo'], 
 'general', 5, TRUE),

-- FAQ GIỜ LÀM VIỆC
('Shop làm việc giờ nào?', 
 E'Chúng tôi hoạt động:\n• Thứ 2 - Chủ nhật: 8:00 - 22:00\n• Hỗ trợ online 24/7 qua chatbot\n• Hotline: 8:00 - 22:00', 
 ARRAY['giờ làm việc', 'thời gian', 'mở cửa', 'đóng cửa', 'working hours', 'giờ mở', 'giờ đóng'], 
 'general', 3, TRUE),

-- FAQ KHU VỰC GIAO HÀNG
('Shop giao hàng khu vực nào?', 
 E'Chúng tôi giao hàng toàn quốc 63 tỉnh thành.\n• Nội thành Hà Nội, TP.HCM: 1-2 ngày\n• Các tỉnh khác: 2-5 ngày làm việc\n• Vùng sâu vùng xa: 5-7 ngày', 
 ARRAY['khu vực', 'vùng', 'tỉnh', 'thành phố', 'giao đâu', 'có giao', 'toàn quốc', 'region', 'area'], 
 'shipping', 5, TRUE),

-- FAQ KIỂM TRA ĐƠN HÀNG
('Làm sao kiểm tra đơn hàng?', 
 E'Bạn có thể kiểm tra đơn hàng bằng cách:\n• Đăng nhập vào tài khoản → Đơn hàng của tôi\n• Tra cứu theo mã đơn hàng tại trang chủ\n• Liên hệ hotline 1900xxxx với mã đơn hàng', 
 ARRAY['kiểm tra đơn hàng', 'tra cứu', 'xem đơn hàng', 'tracking', 'mã vận đơn', 'theo dõi', 'order status'], 
 'general', 5, TRUE);

COMMIT;

-- Kiểm tra kết quả
SELECT id, question, category, priority, is_active 
FROM chatbot_faq 
ORDER BY priority DESC, id ASC;
