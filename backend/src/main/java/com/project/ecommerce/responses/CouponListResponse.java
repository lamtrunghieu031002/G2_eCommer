package com.project.ecommerce.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponListResponse {
    private List<CouponResponse> coupons;
    private int totalPages;
    private long totalElements;
}
