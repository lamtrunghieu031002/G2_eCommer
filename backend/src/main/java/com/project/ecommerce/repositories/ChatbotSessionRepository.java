package com.project.ecommerce.repositories;

import com.project.ecommerce.models.ChatbotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, Long> {
    
    Optional<ChatbotSession> findBySessionId(String sessionId);
    
    Optional<ChatbotSession> findBySessionIdAndIsActiveTrue(String sessionId);
}