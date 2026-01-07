package com.project.ecommerce.dtos.chatbot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponse {
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("message")
    private String message; // Câu trả lời từ bot
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("products")
    private List<ProductSuggestion> products; // Danh sách sản phẩm gợi ý (nếu có)
    
    @JsonProperty("message_type")
    private String messageType; // "text", "product_list", "comparison"
}