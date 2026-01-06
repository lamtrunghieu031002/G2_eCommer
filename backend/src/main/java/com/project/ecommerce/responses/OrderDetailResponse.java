package com.project.ecommerce.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.ecommerce.models.OrderDetail;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailResponse {
    private Long id;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("variant_id")
    private Long variantId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("variant_name")
    private String variantName;

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("price")
    private Float price;

    @JsonProperty("number_of_products")
    private int numberOfProducts;

    @JsonProperty("total_money")
    private Float totalMoney;


    public static OrderDetailResponse fromOrderDetail(OrderDetail orderDetail) {
        return OrderDetailResponse
                .builder()
                .id(orderDetail.getId())
                .orderId(orderDetail.getOrder().getId())
                .variantId(orderDetail.getProductVariant().getId())
                .variantName(orderDetail.getProductVariant().getVariant())
                .productName(orderDetail.getProductVariant().getProduct().getName())
                .thumbnail(orderDetail.getProductVariant().getProduct().getThumbnail())
                .price(orderDetail.getPrice())
                .numberOfProducts(orderDetail.getNumberOfProducts())
                .totalMoney(orderDetail.getTotalMoney())
                .build();
    }
}
