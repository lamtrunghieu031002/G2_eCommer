package com.project.chatbotservice.repositories;

import com.project.chatbotservice.models.ChatbotMessage;
import com.project.chatbotservice.models.ChatbotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {
    
    List<ChatbotMessage> findBySessionOrderByCreatedAtAsc(ChatbotSession session);
    
    List<ChatbotMessage> findTop10BySessionOrderByCreatedAtDesc(ChatbotSession session);
}