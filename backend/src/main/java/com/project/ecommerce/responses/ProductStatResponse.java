package com.project.ecommerce.responses;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductStatResponse{

    private String productName;
    private Long quantitySold;
    private Double revenue;
}
