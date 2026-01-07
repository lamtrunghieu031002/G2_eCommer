package com.project.ecommerce.services.chatbot;

import com.project.ecommerce.dtos.chatbot.ChatRequest;
import com.project.ecommerce.dtos.chatbot.ChatResponse;
import com.project.ecommerce.dtos.chatbot.ProductSuggestion;
import com.project.ecommerce.models.*;
import com.project.ecommerce.repositories.*;
import com.project.ecommerce.services.chatbot.intent.Intent;
import com.project.ecommerce.services.chatbot.intent.IntentDetector;
import com.project.ecommerce.services.chatbot.intent.IntentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotService implements IChatbotService {

    // Repositories
    private final ChatbotSessionRepository sessionRepository;
    private final ChatbotMessageRepository messageRepository;
    private final ChatbotFAQRepository faqRepository;
    private final UserRepository userRepository;
    
    // New Components
    private final IntentDetector intentDetector;
    private final ProductQueryBuilder productQueryBuilder;
    private final ResponseBuilder responseBuilder;
    private final GeminiFormatter geminiFormatter;

    /**
     * XỬ LÝ TIN NHẮN - LUỒNG MỚI
     * Flow: User Message → Intent Detection → DB Query → Build Response → Format → Return
     */
    @Override
    @Transactional
    public ChatResponse processMessage(ChatRequest request) throws Exception {
        System.out.println("\n==================== NEW MESSAGE ====================");
        System.out.println("User Message: " + request.getMessage());
        
        // ✅ THÊM DELAY 2-3 GIÂY ĐỂ TẠO CẢM GIÁC CHUYÊN NGHIỆP
        long delayMillis = 2000 + (long) (Math.random() * 1000); // Random 2000-3000ms
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 1. Lấy hoặc tạo session
        ChatbotSession session = getOrCreateSession(request.getSessionId(), request.getUserId());
        
        // 2. Lưu tin nhắn của user
        saveUserMessage(session, request.getMessage());
        
        // 3. PHÂN TÍCH INTENT (KHÔNG DÙNG AI)
        Intent intent = intentDetector.detectIntent(request.getMessage());
        
        System.out.println("Detected Intent: " + intent.getDescription());
        
        // 4. XỬ LÝ THEO INTENT TYPE
        String responseMessage;
        List<Product> products = new ArrayList<>();
        
        switch (intent.getType()) {
            case GREETING:
                responseMessage = "Xin chào! Tôi là trợ lý mua sắm. Tôi có thể giúp bạn tìm sản phẩm, kiểm tra giá, tình trạng hàng. Bạn cần gì?";
                break;
                
            case GOODBYE:
                responseMessage = "Cảm ơn bạn! Hẹn gặp lại.";
                break;
                
            case FAQ:
                responseMessage = handleFAQ(request.getMessage());
                break;
                
            case UNKNOWN:
                // ✅ CHO PHÉP GEMINI TRẢ LỜI CÂU HỎI CHUNG
                responseMessage = handleGeneralQuestion(request.getMessage());
                break;
                
            default:
                // Các intent liên quan sản phẩm
                responseMessage = handleProductIntent(intent, products);
                break;
        }
        
        // 5. Lưu câu trả lời của bot
        saveBotMessage(session, responseMessage, products);
        
        // 6. Tạo response trả về frontend
        ChatResponse response = createChatResponse(session, responseMessage, products);
        
        System.out.println("Response sent: " + responseMessage.substring(0, Math.min(100, responseMessage.length())) + "...");
        System.out.println("==================== END MESSAGE ====================\n");
        
        return response;
    }
    
    /**
     * Xử lý câu hỏi chung (không liên quan sản phẩm) bằng Gemini
     */
    private String handleGeneralQuestion(String question) {
        try {
            // Tạo prompt cho câu hỏi chung
            String prompt = "Bạn là trợ lý mua sắm thân thiện. Khách hỏi: \"" + question + "\"\n\n" +
                           "Hãy trả lời ngắn gọn (1-2 câu), lịch sự. " +
                           "Nếu không liên quan mua sắm, hướng dẫn họ hỏi về sản phẩm.";
            
            return geminiFormatter.formatResponse(prompt);
        } catch (Exception e) {
            return "Tôi có thể giúp bạn tìm sản phẩm, kiểm tra giá, hoặc tình trạng hàng. Bạn muốn tìm gì?";
        }
    }
    
    /**
     * Xử lý các intent liên quan đến sản phẩm
     */
    private String handleProductIntent(Intent intent, List<Product> products) {
        // 1. QUERY DATABASE (KHÔNG DÙNG AI)
        products.addAll(productQueryBuilder.findProducts(intent));
        
        // 2. XÂY DỰNG RESPONSE CÓ CẤU TRÚC (KHÔNG DÙNG AI)
        String structuredResponse = responseBuilder.buildStructuredResponse(intent, products);
        
        System.out.println("Structured Response:\n" + structuredResponse);
        
        // 3. ✅ KIỂM TRA NẾU LÀ CONTEXT (không có sản phẩm)
        boolean isContextOnly = structuredResponse.startsWith("CONTEXT:");
        
        // 4. FORMAT VỚI GEMINI (CHỈ ĐỂ LÀM ĐẸP CÂU TRẢ LỜI)
        try {
            String formattedResponse = geminiFormatter.formatResponse(structuredResponse);
            
            // ✅ KIỂM TRA NẾU GEMINI TRẢ VỀ CONTEXT GỐC (không format được)
            if (formattedResponse.startsWith("CONTEXT:") || formattedResponse.contains("HÃY TRẢ LỜI TỰ NHIÊN:")) {
                System.err.println("⚠️ Gemini không format được, sử dụng fallback response");
                return buildFallbackResponse(intent, products);
            }
            
            return formattedResponse;
            
        } catch (Exception e) {
            System.err.println("Gemini formatting failed: " + e.getMessage());
            e.printStackTrace();
            
            // ✅ FALLBACK: Trả lời tự nhiên thay vì context kỹ thuật
            return buildFallbackResponse(intent, products);
        }
    }
    
    /**
     * ✅ FALLBACK RESPONSE - Trả lời tự nhiên khi Gemini lỗi
     */
    private String buildFallbackResponse(Intent intent, List<Product> products) {
        // Nếu có sản phẩm → Trả lời đơn giản
        if (!products.isEmpty()) {
            StringBuilder response = new StringBuilder();
            response.append("Shop tìm thấy ").append(products.size()).append(" sản phẩm phù hợp:\n\n");
            
            for (int i = 0; i < Math.min(3, products.size()); i++) {
                Product p = products.get(i);
                response.append("• ").append(p.getName())
                       .append(" - Giá: ").append(formatPrice(p.getPrice()))
                       .append("\n");
            }
            
            response.append("\nBạn muốn xem thêm thông tin sản phẩm nào không ạ?");
            return response.toString();
        }
        
        // Nếu KHÔNG có sản phẩm → Gợi ý thay thế
        StringBuilder response = new StringBuilder();
        response.append("Xin lỗi bạn, hiện tại shop chưa có sản phẩm chính xác như bạn tìm");
        
        // Thêm thông tin tìm kiếm nếu có
        if (intent.getCategory() != null) {
            String categoryVN = mapCategoryToVietnamese(intent.getCategory());
            response.append(" trong danh mục ").append(categoryVN);
        }
        
        if (intent.getBrand() != null) {
            response.append(" của thương hiệu ").append(intent.getBrand());
        }
        
        response.append(".\n\n");
        
        // Gợi ý thay thế
        response.append("Bạn có thể:\n");
        response.append("• Cho mình biết ngân sách của bạn khoảng bao nhiêu\n");
        response.append("• Mô tả chi tiết hơn sản phẩm bạn cần (vd: dùng để làm gì)\n");
        response.append("• Hỏi về các sản phẩm tương tự khác\n\n");
        response.append("Mình sẽ tư vấn sản phẩm phù hợp nhất cho bạn! 😊");
        
        return response.toString();
    }
    
    /**
     * Map category sang tiếng Việt
     */
    private String mapCategoryToVietnamese(String category) {
        switch (category.toLowerCase()) {
            case "phone": return "điện thoại";
            case "laptop": return "laptop";
            case "tablet": return "máy tính bảng";
            case "headphone": return "tai nghe";
            case "watch": return "đồng hồ";
            case "accessories": return "phụ kiện";
            default: return category;
        }
    }
    
    /**
     * Format giá tiền
     */
    private String formatPrice(Float price) {
        if (price >= 1_000_000) {
            float millions = price / 1_000_000;
            return String.format("%.1f triệu", millions);
        } else if (price >= 1_000) {
            return String.format("%.0fk", price / 1_000);
        }
        return String.format("%.0f đ", price);
    }
    
    /**
     * Xử lý FAQ
     */
    private String handleFAQ(String question) {
        List<ChatbotFAQ> faqs = faqRepository.searchByKeyword(question);
        
        if (!faqs.isEmpty()) {
            return responseBuilder.buildFAQResponse(faqs.get(0).getAnswer());
        }
        
        // Fallback FAQ response
        return "Bạn có thể tham khảo các câu hỏi thường gặp:\n" +
               "- Chính sách giao hàng\n" +
               "- Phương thức thanh toán\n" +
               "- Chính sách bảo hành và đổi trả\n\n" +
               "Hoặc liên hệ hotline: 1900xxxx để được hỗ trợ trực tiếp.";
    }

    /**
     * Lấy hoặc tạo session mới
     */
    private ChatbotSession getOrCreateSession(String sessionId, Long userId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            Optional<ChatbotSession> existingSession = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId);
            if (existingSession.isPresent()) {
                return existingSession.get();
            }
        }

        // Tạo session mới
        String newSessionId = UUID.randomUUID().toString();
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        ChatbotSession newSession = ChatbotSession.builder()
                .sessionId(newSessionId)
                .user(user)
                .isActive(true)
                .build();

        return sessionRepository.save(newSession);
    }

    /**
     * Tạo session mới
     */
    @Override
    public String createNewSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        ChatbotSession session = ChatbotSession.builder()
                .sessionId(sessionId)
                .user(user)
                .isActive(true)
                .build();

        sessionRepository.save(session);
        return sessionId;
    }

    /**
     * Lưu tin nhắn của user
     */
    private void saveUserMessage(ChatbotSession session, String message) {
        ChatbotMessage userMessage = ChatbotMessage.builder()
                .session(session)
                .message(message)
                .sender("USER")
                .build();
        messageRepository.save(userMessage);
    }

    /**
     * Lưu câu trả lời của bot
     */
    private void saveBotMessage(ChatbotSession session, String message, List<Product> products) {
        String productIds = null;
        if (products != null && !products.isEmpty()) {
            productIds = products.stream()
                    .map(p -> String.valueOf(p.getId()))
                    .collect(Collectors.joining(","));
        }

        ChatbotMessage botMessage = ChatbotMessage.builder()
                .session(session)
                .message(message)
                .sender("BOT")
                .productIds(productIds)
                .build();
        messageRepository.save(botMessage);
    }

    /**
     * Tạo ChatResponse
     */
    private ChatResponse createChatResponse(ChatbotSession session, String message, List<Product> products) {
        List<ProductSuggestion> suggestions = null;
        String messageType = "text";

        if (products != null && !products.isEmpty()) {
            suggestions = products.stream()
                    .map(this::convertToProductSuggestion)
                    .collect(Collectors.toList());
            messageType = "product_list";
        }

        return ChatResponse.builder()
                .sessionId(session.getSessionId())
                .message(message)
                .timestamp(LocalDateTime.now())
                .products(suggestions)
                .messageType(messageType)
                .build();
    }

    /**
     * Convert Product sang ProductSuggestion
     */
    private ProductSuggestion convertToProductSuggestion(Product product) {
        return ProductSuggestion.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .build();
    }

    /**
     * Lấy lịch sử chat
     */
    @Override
    public ChatResponse getChatHistory(String sessionId) throws Exception {
        ChatbotSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new Exception("Session not found"));

        List<ChatbotMessage> messages = messageRepository.findBySessionOrderByCreatedAtAsc(session);

        // Tạo response với lịch sử
        StringBuilder history = new StringBuilder();
        for (ChatbotMessage msg : messages) {
            history.append(msg.getSender()).append(": ").append(msg.getMessage()).append("\n");
        }

        return ChatResponse.builder()
                .sessionId(sessionId)
                .message(history.toString())
                .timestamp(LocalDateTime.now())
                .messageType("history")
                .build();
    }
}