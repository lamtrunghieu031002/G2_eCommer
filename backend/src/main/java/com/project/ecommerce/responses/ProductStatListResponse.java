package com.project.ecommerce.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ProductStatListResponse {

    private List<ProductStatResponse> products;
    private int totalPages;
}
