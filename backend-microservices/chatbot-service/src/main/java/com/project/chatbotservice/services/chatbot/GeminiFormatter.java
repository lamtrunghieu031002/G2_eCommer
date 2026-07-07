package com.project.chatbotservice.services.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.chatbotservice.configurations.GeminiConfig;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component sử dụng Gemini API CHỈ ĐỂ FORMAT câu trả lời
 * Gemini KHÔNG ĐƯỢC tự bịa thông tin, chỉ được viết lại thành câu tự nhiên
 */
@Component
@RequiredArgsConstructor
public class GeminiFormatter {
    
    private final OkHttpClient okHttpClient;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;
    private final HallucinationDetector hallucinationDetector;
    
    /**
     * Format structured response thành câu trả lời tự nhiên
     * Sử dụng Gemini với temperature thấp để giữ chính xác thông tin
     */
    public String formatResponse(String structuredResponse) throws Exception {
        System.out.println("===== GEMINI FORMATTER =====");
        System.out.println("Input length: " + structuredResponse.length() + " chars");
        
        try {
            // ✅ SYSTEM PROMPT THEO YÊU CẦU - "KHÔNG BỎ RƠI KHÁCH HÀNG"
            String systemPrompt = buildSystemPrompt();
            
            // Tạo full prompt
            String fullPrompt = systemPrompt + "\n\n" + 
                               "===== DỮ LIỆU TỪ DATABASE =====\n" +
                               structuredResponse + "\n\n" +
                               "===== YÊU CẦU =====\n" +
                               "Hãy chuyển dữ liệu trên thành câu trả lời tự nhiên, thân thiện.\n" +
                               "- Giữ CHÍNH XÁC tên sản phẩm, giá tiền, số lượng.\n" +
                               "- Nếu không có sản phẩm, hãy gợi ý thay thế hoặc hỏi thêm thông tin.\n" +
                               "- Đừng tự bịa thông tin không có trong dữ liệu.\n" +
                               "- Trả lời ngắn gọn, súc tích (không quá 200 từ).";
            
            // ✅ Gọi Gemini API với HTTP Request
            String formattedResponse = callGeminiAPI(fullPrompt);
            
            System.out.println("Gemini response length: " + formattedResponse.length() + " chars");
            
            // ✅ HALLUCINATION DETECTION - 5 LỚP KIỂM TRA
            if (!hallucinationDetector.isResponseValid(structuredResponse, formattedResponse)) {
                System.err.println("⚠️ HALLUCINATION DETECTED! Using structured response instead.");
                return convertStructuredToPlainText(structuredResponse);
            }
            
            System.out.println("✓ Response validated successfully");
            System.out.println("===== FORMATTER COMPLETE =====\n");
            
            return formattedResponse;
            
        } catch (Exception e) {
            System.err.println("Error calling Gemini: " + e.getMessage());
            // Fallback: trả về structured response
            return convertStructuredToPlainText(structuredResponse);
        }
    }
    
    /**
     * Gọi Gemini API thực tế
     */
    private String callGeminiAPI(String prompt) throws Exception {
        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        
        // Contents
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        
        List<Map<String, String>> requestParts = new ArrayList<>();  // ✅ ĐỔI TÊN: parts → requestParts
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        requestParts.add(part);
        
        content.put("parts", requestParts);
        contents.add(content);
        requestBody.put("contents", contents);
        
        // Generation config với temperature THẤP để giữ chính xác
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.1);  // Temperature thấp = ít hallucination
        generationConfig.put("maxOutputTokens", 1000);
        requestBody.put("generationConfig", generationConfig);
        
        // Convert to JSON
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        // Create HTTP request
        String url = geminiConfig.getApiUrl() + "?key=" + geminiConfig.getApiKey();
        
        RequestBody body = RequestBody.create(
            jsonBody, 
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build();
        
        // Execute request
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Gemini API error: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            
            // Extract text from response
            JsonNode candidates = jsonResponse.get("candidates");
            if (candidates != null && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode responseContent = firstCandidate.get("content");  // ✅ ĐỔI TÊN: content → responseContent
                if (responseContent != null) {
                    JsonNode responseParts = responseContent.get("parts");  // ✅ ĐỔI TÊN: parts → responseParts
                    if (responseParts != null && responseParts.size() > 0) {
                        JsonNode text = responseParts.get(0).get("text");
                        if (text != null) {
                            return text.asText().trim();
                        }
                    }
                }
            }
            
            throw new Exception("Failed to extract text from Gemini response");
        }
    }
    
    /**
     * Convert structured response thành plain text
     * Sử dụng khi Gemini lỗi hoặc phát hiện hallucination
     */
    private String convertStructuredToPlainText(String structuredResponse) {
        // Remove technical markers
        String cleaned = structuredResponse
            .replace("INTENT:", "")
            .replace("QUERY:", "")
            .replace("SẢN PHẨM:", "📱")
            .replace("GIÁ:", "💰 Giá:")
            .replace("TÌNH TRẠNG:", "📦")
            .replace("CÁC PHIÊN BẢN:", "✨ Phiên bản:");
        
        // Trim multiple newlines
        cleaned = cleaned.replaceAll("\n{3,}", "\n\n");
        
        return cleaned.trim();
    }
    
    /**
     * ✅ SYSTEM PROMPT - NGUYÊN TẮC CHATBOT
     */
    private String buildSystemPrompt() {
        return """
            ===== VAI TRÒ =====
            Bạn là NHÂN VIÊN TƯ VẤN BÁN HÀNG CHUYÊN NGHIỆP, THÂN THIỆN của một cửa hàng điện tử.
            
            ===== NGUYÊN TẮC "KHÔNG BỎ RƠI KHÁCH HÀNG" =====
            
            1. NHẬN DIỆN Ý ĐỊNH:
               - Nếu khách chào hỏi/hỏi thăm (VD: "Bạn khỏe không?"):
                 → Trả lời LỄ PHÉP như con người, sau đó hỏi họ cần hỗ trợ gì về sản phẩm.
               
               - Nếu khách hỏi về QUY TRÌNH (VD: "Cách mua hàng", "Thanh toán"):
                 → TUYỆT ĐỐI không tìm trong bảng Sản phẩm.
                 → Trả lời dựa trên dữ liệu FAQ được cung cấp.
               
               - Nếu khách hỏi sản phẩm CHUNG CHUNG (VD: "Điện thoại giá rẻ"):
                 → Đừng báo lỗi ngay.
                 → Liệt kê các sản phẩm thuộc danh mục đó hoặc HỎI THÊM về ngân sách.
            
            2. KHI KHÔNG TÌM THẤY SẢN PHẨM CỤ THỂ:
               ❌ ĐỪNG NÓI: "Không tìm thấy sản phẩm"
               ✅ HÃY NÓI: "Hiện tại mẫu này shop đang hết hàng, nhưng shop có [Sản phẩm A] hoặc [Sản phẩm B] cùng tầm giá, bạn có muốn xem thử không?"
               
               - Nếu có thông tin về category/brand: Gợi ý sản phẩm TƯƠNG TỰ
               - Nếu có thông tin về ngân sách: Hỏi khách có thể tăng ngân sách không
               - Nếu không đủ thông tin: HỎI THÊM về nhu cầu sử dụng (làm việc, chơi game, học tập...)
            
            3. KHI KHÁCH HỎI TƯ VẤN:
               - Hỏi nhu cầu sử dụng: Làm việc văn phòng? Chơi game? Chụp ảnh?
               - Hỏi ngân sách: Khoảng bao nhiêu tiền?
               - Hỏi thương hiệu ưa thích: Apple? Samsung? Xiaomi?
               
            4. PHẢN HỒI FAQ:
               - Khi trả lời FAQ, đừng lặp lại câu hỏi.
               - Trả lời NGẮN GỌN, CHI TIẾT nội dung.
               
            ===== MAPPING TỪ KHÓA =====
            - Máy tính = Laptop = Macbook
            - Điện thoại = Smartphone = iPhone/Samsung
            - Tai nghe = Headphone = Earphone
            
            ===== YÊU CẦU QUAN TRỌNG =====
            - KHÔNG TỰ BỊA thông tin không có trong dữ liệu
            - GIỮ CHÍNH XÁC tên sản phẩm, giá tiền
            - Trả lời NGẮN GỌN, tối đa 200 từ
            - Dùng emoji phù hợp (😊, ✨, 🎯) để thân thiện hơn
            """;
    }
}
