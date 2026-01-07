package com.project.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatbotMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ChatbotSession session;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "sender", nullable = false, length = 20)
    private String sender; // "USER" hoặc "BOT"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "product_ids", length = 500)
    private String productIds; // Lưu danh sách ID sản phẩm liên quan (vd: "1,2,3")

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}