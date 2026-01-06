package com.project.ecommerce.services.review;

import com.project.ecommerce.dtos.review.InsertReviewDTO;
import com.project.ecommerce.dtos.review.UpdateReviewDTO;
import com.project.ecommerce.models.Review;
import com.project.ecommerce.responses.ReviewListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IReviewService {
    Review createReview(InsertReviewDTO insertReviewDTO, Long userId);
    Review updateReview(Long reviewId, UpdateReviewDTO updateReviewDTO, Long userId);
    void deleteReview(Long reviewId, Long userId);
    Review getReviewById(Long reviewId);
    Page<Review> getReviewsByProductId(Long productId, Pageable pageable);
    List<Review> getReviewsByUserId(Long userId);
    boolean hasUserReviewedProduct(Long userId, Long productId);
    Double getAverageRatingByProductId(Long productId);
    Long getReviewCountByProductId(Long productId);
    List<Object[]> getRatingDistributionByProductId(Long productId);
}

