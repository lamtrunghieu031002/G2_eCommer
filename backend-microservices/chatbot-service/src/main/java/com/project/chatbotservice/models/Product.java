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
@EqualsAndHashCode(of = "id")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private Long id;
    private String name;
    private Float price;
    private String thumbnail;
    private String description;
    private Category category;
}
