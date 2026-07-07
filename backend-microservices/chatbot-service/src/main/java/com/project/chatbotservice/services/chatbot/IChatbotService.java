package com.project.chatbotservice.services.chatbot;

import com.project.chatbotservice.dtos.chatbot.ChatRequest;
import com.project.chatbotservice.dtos.chatbot.ChatResponse;

public interface IChatbotService {
    
    /**
     * Xử lý tin nhắn từ user và trả về câu trả lời
     */
    ChatResponse processMessage(ChatRequest request) throws Exception;
    
    /**
     * Tạo session mới cho cuộc hội thoại
     */
    String createNewSession(Long userId);
    
    /**
     * Lấy lịch sử chat của một session
     */
    ChatResponse getChatHistory(String sessionId) throws Exception;
}