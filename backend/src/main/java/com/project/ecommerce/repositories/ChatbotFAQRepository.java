package com.project.ecommerce.repositories;

import com.project.ecommerce.models.ChatbotFAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatbotFAQRepository extends JpaRepository<ChatbotFAQ, Long> {
    
    List<ChatbotFAQ> findByIsActiveTrueOrderByPriorityDesc();
    
    @Query("SELECT f FROM ChatbotFAQ f WHERE f.isActive = true AND " +
           "LOWER(f.question) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ChatbotFAQ> searchByKeyword(@Param("keyword") String keyword);
}