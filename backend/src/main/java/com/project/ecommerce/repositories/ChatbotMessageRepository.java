package com.project.ecommerce.repositories;

import com.project.ecommerce.models.ChatbotMessage;
import com.project.ecommerce.models.ChatbotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {
    
    List<ChatbotMessage> findBySessionOrderByCreatedAtAsc(ChatbotSession session);
    
    List<ChatbotMessage> findTop10BySessionOrderByCreatedAtDesc(ChatbotSession session);
}