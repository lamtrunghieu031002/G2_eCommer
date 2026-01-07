package com.project.ecommerce.repositories;

import com.project.ecommerce.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Tìm review theo product ID
    Page<Review> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    // Tìm review theo user ID
    List<Review> findByUserId(Long userId);

    // Kiểm tra user đã review sản phẩm này chưa
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // Tìm review theo user và product
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    // Tính rating trung bình của sản phẩm
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    // Đếm số lượng review của sản phẩm
    Long countByProductId(Long productId);

    // Lấy phân phối rating (1-5 sao) của sản phẩm
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") Long productId);
}



