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
public class ProductVariant {
    private Long id;
    private String variant;
    private int stock;
    private Float price;

    // Giu cac getter tuong thich voi code chatbot cu
    public String getName() {
        return this.variant;
    }

    public int getQuantity() {
        return this.stock;
    }
}
