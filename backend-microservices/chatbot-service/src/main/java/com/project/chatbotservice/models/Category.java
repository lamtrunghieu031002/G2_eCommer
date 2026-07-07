package com.project.chatbotservice.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * POJO nhan du lieu tu product-service (khong phai JPA entity).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    private Long id;
    private String name;
}
