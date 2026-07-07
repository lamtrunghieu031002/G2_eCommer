package com.project.productservice.services.review;

import com.project.productservice.clients.UserClient;
import com.project.productservice.dtos.review.InsertReviewDTO;
import com.project.productservice.dtos.review.UpdateReviewDTO;
import com.project.productservice.exceptions.DataNotFoundException;
import com.project.productservice.exceptions.InvalidParamException;
import com.project.productservice.models.Product;
import com.project.productservice.models.Review;
import com.project.productservice.repositories.ProductRepository;
import com.project.productservice.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final UserClient userClient;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Review createReview(InsertReviewDTO insertReviewDTO, Long userId) {
        // Kiểm tra user tồn tại (goi user-service)
        String userName = userClient.getUserName(userId);
        if (userName == null) {
            throw new DataNotFoundException("User not found with id: " + userId);
        }

        // Kiểm tra product tồn tại
        Product product = productRepository.findById(insertReviewDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + insertReviewDTO.getProductId()));

        // Kiểm tra user đã review sản phẩm này chưa
        if (reviewRepository.existsByUserIdAndProductId(userId, insertReviewDTO.getProductId())) {
            throw new InvalidParamException("User has already reviewed this product");
        }

        // Tạo review mới (luu snapshot ten user)
        Review review = Review.builder()
                .userId(userId)
                .userName(userName)
                .product(product)
                .rating(insertReviewDTO.getRating())
                .comment(insertReviewDTO.getComment())
                .build();

        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review updateReview(Long reviewId, UpdateReviewDTO updateReviewDTO, Long userId) {
        // Tìm review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DataNotFoundException("Review not found with id: " + reviewId));

        // Kiểm tra quyền sở hữu
        if (!review.getUserId().equals(userId)) {
            throw new InvalidParamException("You can only update your own reviews");
        }

        // Cập nhật review
        review.setRating(updateReviewDTO.getRating());
        review.setComment(updateReviewDTO.getComment());

        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        // Tìm review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DataNotFoundException("Review not found with id: " + reviewId));

        // Kiểm tra quyền sở hữu
        if (!review.getUserId().equals(userId)) {
            throw new InvalidParamException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    @Override
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DataNotFoundException("Review not found with id: " + reviewId));
    }

    @Override
    public Page<Review> getReviewsByProductId(Long productId, Pageable pageable) {
        // Kiểm tra product tồn tại
        if (!productRepository.existsById(productId)) {
            throw new DataNotFoundException("Product not found with id: " + productId);
        }

        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
    }

    @Override
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Override
    public boolean hasUserReviewedProduct(Long userId, Long productId) {
        return reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public Double getAverageRatingByProductId(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }

    @Override
    public Long getReviewCountByProductId(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    @Override
    public List<Object[]> getRatingDistributionByProductId(Long productId) {
        return reviewRepository.getRatingDistributionByProductId(productId);
    }
}



