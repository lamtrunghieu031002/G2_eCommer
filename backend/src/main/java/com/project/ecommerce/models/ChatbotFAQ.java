package com.project.ecommerce.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chatbot_faq")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatbotFAQ {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT", nullable = false)
    private String answer;

    @Column(name = "category", length = 50)
    private String category; // vd: "shipping", "payment", "product"

    @Column(name = "priority")
    private Integer priority; // Độ ưu tiên hiển thị

    @Column(name = "is_active")
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
    }
}