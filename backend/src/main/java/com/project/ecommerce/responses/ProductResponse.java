package com.project.ecommerce.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.ecommerce.models.Product;
import com.project.ecommerce.models.ProductImage;
import com.project.ecommerce.models.ProductVariant;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse extends BaseResponse{
    private Long id;
    private String name;
    private Float price;
    private String thumbnail;
    private String description;
    @JsonProperty("product_images")
    private Set<ProductImage> productImages = new HashSet<>();

    @JsonProperty("product_variants")
    private Set<ProductVariant> variants = new HashSet<>();

    @JsonProperty("category_id")
    private Long categoryId;
    public static ProductResponse fromProduct(Product product) {
        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .productImages(product.getProductImages())
                .variants(product.getProductVariants())
                .build();
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        return productResponse;
    }
}
