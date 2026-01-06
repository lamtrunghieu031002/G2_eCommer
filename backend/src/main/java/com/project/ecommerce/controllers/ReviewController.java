package com.project.ecommerce.controllers;

import com.project.ecommerce.dtos.review.InsertReviewDTO;
import com.project.ecommerce.dtos.review.UpdateReviewDTO;
import com.project.ecommerce.models.Review;
import com.project.ecommerce.responses.ResponseObject;
import com.project.ecommerce.responses.ReviewListResponse;
import com.project.ecommerce.responses.ReviewResponse;
import com.project.ecommerce.services.review.IReviewService;
import com.project.ecommerce.components.JwtTokenUtils;
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
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;
    private final JwtTokenUtils jwtTokenUtils;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> createReview(
            @Valid @RequestBody InsertReviewDTO insertReviewDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String token = authorizationHeader.substring(7);
            String phoneNumber = jwtTokenUtils.extractPhoneNumber(token);
            Long userId = jwtTokenUtils.extractUserId(token);

            Review review = reviewService.createReview(insertReviewDTO, userId);
            ReviewResponse reviewResponse = ReviewResponse.fromReview(review);

            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.CREATED)
                    .message("Review created successfully!")
                    .data(reviewResponse)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewDTO updateReviewDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String token = authorizationHeader.substring(7);
            Long userId = jwtTokenUtils.extractUserId(token);

            Review review = reviewService.updateReview(reviewId, updateReviewDTO, userId);
            ReviewResponse reviewResponse = ReviewResponse.fromReview(review);

            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Review updated successfully!")
                    .data(reviewResponse)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String token = authorizationHeader.substring(7);
            Long userId = jwtTokenUtils.extractUserId(token);

            reviewService.deleteReview(reviewId, userId);

            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Review deleted successfully!")
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProductId(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        try {
            Pageable pageable = PageRequest.of(
                    page, limit,
                    Sort.by("createdAt").descending()
            );

            Page<Review> reviewPage = reviewService.getReviewsByProductId(productId, pageable);
            List<ReviewResponse> reviewResponses = reviewPage.getContent().stream()
                    .map(ReviewResponse::fromReview)
                    .collect(Collectors.toList());

            ReviewListResponse reviewListResponse = ReviewListResponse.builder()
                    .reviews(reviewResponses)
                    .totalPages(reviewPage.getTotalPages())
                    .totalElements(reviewPage.getTotalElements())
                    .build();

            return ResponseEntity.ok(reviewListResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<?> getProductReviewStats(@PathVariable Long productId) {
        try {
            Double averageRating = reviewService.getAverageRatingByProductId(productId);
            Long reviewCount = reviewService.getReviewCountByProductId(productId);
            List<Object[]> ratingDistribution = reviewService.getRatingDistributionByProductId(productId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("averageRating", averageRating != null ? averageRating : 0.0);
            stats.put("reviewCount", reviewCount);
            stats.put("ratingDistribution", ratingDistribution);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> getUserReviews(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        try {
            String token = authorizationHeader.substring(7);
            Long userId = jwtTokenUtils.extractUserId(token);

            List<Review> reviews = reviewService.getReviewsByUserId(userId);
            List<ReviewResponse> reviewResponses = reviews.stream()
                    .map(ReviewResponse::fromReview)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(reviewResponses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/check/{productId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> hasUserReviewedProduct(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String token = authorizationHeader.substring(7);
            Long userId = jwtTokenUtils.extractUserId(token);

            boolean hasReviewed = reviewService.hasUserReviewedProduct(userId, productId);

            Map<String, Object> response = new HashMap<>();
            response.put("hasReviewed", hasReviewed);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
