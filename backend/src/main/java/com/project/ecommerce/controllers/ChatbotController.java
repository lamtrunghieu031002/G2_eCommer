package com.project.ecommerce.controllers;

import com.project.ecommerce.dtos.chatbot.ChatRequest;
import com.project.ecommerce.dtos.chatbot.ChatResponse;
import com.project.ecommerce.responses.ResponseObject;
import com.project.ecommerce.services.chatbot.IChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final IChatbotService chatbotService;

    /**
     * API chính - Gửi tin nhắn và nhận phản hồi từ chatbot
     * POST /api/v1/chatbot/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<ResponseObject> chat(
            @Valid @RequestBody ChatRequest request
    ) {
        try {
            ChatResponse response = chatbotService.processMessage(request);
            
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Chat processed successfully")
                    .data(response)
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Error processing chat: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    /**
     * API tạo session mới
     * POST /api/v1/chatbot/session/new
     */
    @PostMapping("/session/new")
    public ResponseEntity<ResponseObject> createNewSession(
            @RequestParam(required = false) Long userId
    ) {
        try {
            String sessionId = chatbotService.createNewSession(userId);
            
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Session created successfully")
                    .data(sessionId)
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Error creating session: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    /**
     * API lấy lịch sử chat
     * GET /api/v1/chatbot/history/{sessionId}
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<ResponseObject> getChatHistory(
            @PathVariable String sessionId
    ) {
        try {
            ChatResponse history = chatbotService.getChatHistory(sessionId);
            
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Chat history retrieved successfully")
                    .data(history)
                    .build());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Error retrieving history: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    /**
     * API health check cho chatbot
     * GET /api/v1/chatbot/health
     */
    @GetMapping("/health")
    public ResponseEntity<ResponseObject> healthCheck() {
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Chatbot service is running")
                .data("OK")
                .build());
    }
}