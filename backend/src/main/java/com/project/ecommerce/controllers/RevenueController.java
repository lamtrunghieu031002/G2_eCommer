package com.project.ecommerce.controllers;


import com.project.ecommerce.responses.ProductStatListResponse;
import com.project.ecommerce.responses.ProductStatResponse;
import com.project.ecommerce.responses.ResponseObject;
import com.project.ecommerce.services.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/revenues")
@RequiredArgsConstructor
public class RevenueController {
    private final IProductService productService;

    @GetMapping("/by-product")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> getRevenueByProduct(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {

        PageRequest pageRequest = PageRequest.of(page, limit);
        int totalPages = 0;
        Page<ProductStatResponse> responses = productService.getProductStat(start, end, pageRequest);
        totalPages = responses.getTotalPages();

        ProductStatListResponse listResponse = ProductStatListResponse.builder()
                .products(responses.getContent())
                .totalPages(totalPages).build();

        return ResponseEntity.ok(ResponseObject.builder()
                .data(listResponse)
                .status(HttpStatus.OK).build());
    }
}
