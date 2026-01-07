package com.project.ecommerce.services.coupon;

import com.project.ecommerce.models.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ICouponService {

    Coupon createCoupon(Coupon coupon);

    Optional<Coupon> getCouponById(Long id);

    Optional<Coupon> getCouponByCode(String code);

    Page<Coupon> getAllCoupons(String keyword, Boolean active, Pageable pageable);

    Coupon updateCoupon(Long id, Coupon coupon);

    void deleteCoupon(Long id);

    boolean isCouponCodeExists(String code);

    List<Coupon> getValidCoupons();

    void incrementCouponUsage(String code);

    boolean validateCoupon(String code, Double orderAmount);
}



