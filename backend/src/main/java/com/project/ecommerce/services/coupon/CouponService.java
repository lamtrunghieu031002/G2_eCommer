package com.project.ecommerce.services.coupon;

import com.project.ecommerce.exceptions.DataNotFoundException;
import com.project.ecommerce.models.Coupon;
import com.project.ecommerce.repositories.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        if (couponRepository.existsByCode(coupon.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists: " + coupon.getCode());
        }
        return couponRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> getCouponById(Long id) {
        return couponRepository.findById(id);
    }

    @Override
    public Optional<Coupon> getCouponByCode(String code) {
        return couponRepository.findByCode(code);
    }

    @Override
    public Page<Coupon> getAllCoupons(String keyword, Boolean active, Pageable pageable) {
        return couponRepository.findAll(keyword, active, pageable);
    }

    @Override
    @Transactional
    public Coupon updateCoupon(Long id, Coupon coupon) {
        Coupon existingCoupon = couponRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Coupon not found with id: " + id));

        // Check if code is being changed and if it conflicts
        if (!existingCoupon.getCode().equals(coupon.getCode()) &&
            couponRepository.existsByCode(coupon.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists: " + coupon.getCode());
        }

        // Update fields
        existingCoupon.setCode(coupon.getCode());
        existingCoupon.setName(coupon.getName());
        existingCoupon.setDescription(coupon.getDescription());
        existingCoupon.setDiscountType(coupon.getDiscountType());
        existingCoupon.setDiscountValue(coupon.getDiscountValue());
        existingCoupon.setMinimumOrderAmount(coupon.getMinimumOrderAmount());
        existingCoupon.setMaximumDiscount(coupon.getMaximumDiscount());
        existingCoupon.setStartDate(coupon.getStartDate());
        existingCoupon.setEndDate(coupon.getEndDate());
        existingCoupon.setActive(coupon.isActive());
        existingCoupon.setUsageLimit(coupon.getUsageLimit());
        existingCoupon.setUsedCount(coupon.getUsedCount());

        return couponRepository.save(existingCoupon);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new DataNotFoundException("Coupon not found with id: " + id);
        }
        couponRepository.deleteById(id);
    }

    @Override
    public boolean isCouponCodeExists(String code) {
        return couponRepository.existsByCode(code);
    }

    @Override
    public List<Coupon> getValidCoupons() {
        return couponRepository.findValidCoupons(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void incrementCouponUsage(String code) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(code);
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            coupon.incrementUsedCount();
            couponRepository.save(coupon);
        }
    }

    @Override
    public boolean validateCoupon(String code, Double orderAmount) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(code);
        if (couponOpt.isEmpty()) {
            return false;
        }

        Coupon coupon = couponOpt.get();
        if (!coupon.isValid()) {
            return false;
        }

        // Check minimum order amount
        if (coupon.getMinimumOrderAmount() != null &&
            BigDecimal.valueOf(orderAmount).compareTo(coupon.getMinimumOrderAmount()) < 0) {
            return false;
        }

        return true;
    }
}



