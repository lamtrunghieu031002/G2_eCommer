package com.project.ecommerce.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.ecommerce.models.Review;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse extends BaseResponse {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("product_id")
    private Long productId;

    private Integer rating;

    private String comment;

    public static ReviewResponse fromReview(Review review) {
        ReviewResponse reviewResponse = ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .productId(review.getProduct().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .build();
        reviewResponse.setCreatedAt(review.getCreatedAt());
        reviewResponse.setUpdatedAt(review.getUpdatedAt());
        return reviewResponse;
    }
}
