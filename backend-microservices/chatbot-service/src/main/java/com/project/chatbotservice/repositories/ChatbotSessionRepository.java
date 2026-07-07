package com.project.chatbotservice.repositories;

import com.project.chatbotservice.models.ChatbotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, Long> {
    
    Optional<ChatbotSession> findBySessionId(String sessionId);
    
    Optional<ChatbotSession> findBySessionIdAndIsActiveTrue(String sessionId);
}