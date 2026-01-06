package com.project.ecommerce.dtos.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariantDTO {

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("variant")
    @NotBlank(message = "variant is required")
    private String variant;

    @JsonProperty("stock")
    @NotNull(message = "Stock is required")
    private int stock;
}
