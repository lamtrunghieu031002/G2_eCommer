package com.project.orderservice.controllers;

import com.project.orderservice.responses.ProductStatListResponse;
import com.project.orderservice.responses.ProductStatResponse;
import com.project.orderservice.responses.ResponseObject;
import com.project.orderservice.services.revenue.RevenueService;
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

@RestController
@RequestMapping("${api.prefix}/revenues")
@RequiredArgsConstructor
public class RevenueController {
    private final RevenueService revenueService;

    @GetMapping("/by-product")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> getRevenueByProduct(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<ProductStatResponse> responses = revenueService.getProductStat(start, end, pageRequest);

        ProductStatListResponse listResponse = ProductStatListResponse.builder()
                .products(responses.getContent())
                .totalPages(responses.getTotalPages()).build();

        return ResponseEntity.ok(ResponseObject.builder()
                .data(listResponse)
                .status(HttpStatus.OK).build());
    }
}
