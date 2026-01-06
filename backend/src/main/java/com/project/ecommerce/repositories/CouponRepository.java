package com.project.ecommerce.repositories;

import com.project.ecommerce.models.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    Page<Coupon> findAll(Pageable pageable);

    @Query("SELECT c FROM Coupon c WHERE " +
           "(:keyword IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:active IS NULL OR c.active = :active)")
    Page<Coupon> findAll(@Param("keyword") String keyword,
                        @Param("active") Boolean active,
                        Pageable pageable);

    @Query("SELECT c FROM Coupon c WHERE " +
           "c.active = true AND " +
           "c.startDate <= :now AND " +
           "c.endDate >= :now AND " +
           "(c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    List<Coupon> findValidCoupons(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(c) FROM Coupon c WHERE " +
           "c.active = true AND " +
           "c.startDate <= :now AND " +
           "c.endDate >= :now")
    long countActiveCoupons(@Param("now") LocalDateTime now);
}
