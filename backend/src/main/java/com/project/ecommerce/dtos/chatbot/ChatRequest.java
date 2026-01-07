package com.project.ecommerce.dtos.chatbot;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRequest {
    
    @JsonProperty("session_id")
    private String sessionId; // Có thể null nếu là chat mới
    
    @NotBlank(message = "Message không được để trống")
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("user_id")
    private Long userId; // Có thể null nếu là guest
}