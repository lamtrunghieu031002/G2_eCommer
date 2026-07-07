package com.project.orderservice.dtos.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CartItemDTO {

    @JsonProperty("variant_id")
    private Long variantId;

    @JsonProperty("quantity")
    private Integer quantity;
}
