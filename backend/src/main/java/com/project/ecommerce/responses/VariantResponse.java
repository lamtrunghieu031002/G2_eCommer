package com.project.ecommerce.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.ecommerce.models.ProductVariant;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VariantResponse {

    private Long id;
    @JsonProperty("variant_name")
    private String variantName;
    @JsonProperty("price")
    private Float price;
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("thumbnail")
    private String thumbnail;

    public static VariantResponse fromVariant(ProductVariant variant){
        return VariantResponse.builder()
                .id(variant.getId())
                .thumbnail(variant.getProduct().getThumbnail())
                .variantName(variant.getVariant())
                .price(variant.getProduct().getPrice())
                .productName(variant.getProduct().getName())
                .build();
    }
}
