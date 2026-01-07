package com.project.ecommerce.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Giữ tên field "variant" trong Java, map với cột "name" trong DB
    @Column(name = "name")
    private String variant;

    // ✅ Giữ tên field "stock" trong Java, map với cột "quantity" trong DB
    @Column(name = "quantity")
    private Integer stock;
    
    // ✅ Thêm field price (map với cột "price" trong DB)
    @Column(name = "price")
    private Float price;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;
    
    // ✅ THÊM GETTER CHO CHATBOT (alias methods)
    /**
     * Alias method for chatbot - returns variant name
     */
    public String getName() {
        return this.variant;
    }
    
    /**
     * Alias method for chatbot - returns stock quantity
     */
    public Integer getQuantity() {
        return this.stock;
    }
}
