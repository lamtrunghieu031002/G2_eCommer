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

    @Column(name = "name")
    private String variant;

    @Column(name = "quantity")
    private int stock;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    // Custom getters for compatibility with chatbot code
    public String getName() {
        return this.variant;
    }

    public int getQuantity() {
        return this.stock;
    }

    public Float getPrice() {
        return this.product != null ? this.product.getPrice() : null;
    }

}
