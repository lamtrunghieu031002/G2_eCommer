package com.project.ecommerce.controllers;

import com.project.ecommerce.dtos.coupon.CouponDTO;
import com.project.ecommerce.dtos.coupon.UpdateCouponDTO;
import com.project.ecommerce.models.Coupon;
import com.project.ecommerce.responses.CouponListResponse;
import com.project.ecommerce.responses.CouponResponse;
import com.project.ecommerce.services.coupon.ICouponService;
import com.project.ecommerce.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;

    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllCoupons(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "true", required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        try {
            Pageable pageable = PageRequest.of(
                    page, limit,
                    Sort.by("createdAt").descending()
            );

            Page<Coupon> couponPage = couponService.getAllCoupons(keyword, active, pageable);
            List<CouponResponse> couponResponses = couponPage.getContent().stream()
                    .map(CouponResponse::fromCoupon)
                    .collect(Collectors.toList());

            CouponListResponse couponListResponse = CouponListResponse.builder()
                    .coupons(couponResponses)
                    .totalPages(couponPage.getTotalPages())
                    .totalElements(couponPage.getTotalElements())
                    .build();

            return ResponseEntity.ok().body(couponListResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getCouponById(@PathVariable Long id) {
        try {
            Coupon coupon = couponService.getCouponById(id)
                    .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

            CouponResponse couponResponse = CouponResponse.fromCoupon(coupon);
            return ResponseEntity.ok(couponResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createCoupon(@Valid @RequestBody CouponDTO couponDTO) {
        try {
            Coupon coupon = new Coupon();
            coupon.setCode(couponDTO.getCode());
            coupon.setName(couponDTO.getName());
            coupon.setDescription(couponDTO.getDescription());
            coupon.setDiscountType(couponDTO.getDiscountType());
            coupon.setDiscountValue(couponDTO.getDiscountValue());
            coupon.setMinimumOrderAmount(couponDTO.getMinimumOrderAmount());
            coupon.setMaximumDiscount(couponDTO.getMaximumDiscount());
            coupon.setStartDate(couponDTO.getStartDate());
            coupon.setEndDate(couponDTO.getEndDate());
            coupon.setActive(couponDTO.getActive() != null ? couponDTO.getActive() : true);
            coupon.setUsageLimit(couponDTO.getUsageLimit());

            Coupon createdCoupon = couponService.createCoupon(coupon);
            CouponResponse couponResponse = CouponResponse.fromCoupon(createdCoupon);

            return ResponseEntity.status(HttpStatus.CREATED).body(couponResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateCoupon(@PathVariable Long id, @Valid @RequestBody UpdateCouponDTO couponDTO) {
        try {
            Coupon coupon = new Coupon();
            coupon.setCode(couponDTO.getCode());
            coupon.setName(couponDTO.getName());
            coupon.setDescription(couponDTO.getDescription());
            coupon.setDiscountType(couponDTO.getDiscountType());
            coupon.setDiscountValue(couponDTO.getDiscountValue());
            coupon.setMinimumOrderAmount(couponDTO.getMinimumOrderAmount());
            coupon.setMaximumDiscount(couponDTO.getMaximumDiscount());
            coupon.setStartDate(couponDTO.getStartDate());
            coupon.setEndDate(couponDTO.getEndDate());
            coupon.setActive(couponDTO.getActive());
            coupon.setUsageLimit(couponDTO.getUsageLimit());

            Coupon updatedCoupon = couponService.updateCoupon(id, coupon);
            CouponResponse couponResponse = CouponResponse.fromCoupon(updatedCoupon);

            return ResponseEntity.ok(couponResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
            return ResponseEntity.ok().body("Coupon deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<?> validateCoupon(@PathVariable String code, @RequestParam Double orderAmount) {
        try {
            boolean isValid = couponService.validateCoupon(code, orderAmount);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);

            if (isValid) {
                Coupon coupon = couponService.getCouponByCode(code).get();
                response.put("coupon", CouponResponse.fromCoupon(coupon));
                response.put("message", "Coupon is valid");
            } else {
                response.put("message", "Coupon is invalid or expired");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
