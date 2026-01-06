package com.project.ecommerce.dtos.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCouponDTO {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
    private String code;

    @NotBlank(message = "Coupon name is required")
    @Size(max = 255, message = "Coupon name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Discount type is required")
    @Pattern(regexp = "PERCENT|FIXED", message = "Discount type must be either PERCENT or FIXED")
    private String discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Minimum order amount must be non-negative")
    private BigDecimal minimumOrderAmount;

    @DecimalMin(value = "0.0", message = "Maximum discount must be non-negative")
    private BigDecimal maximumDiscount;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    private Boolean active;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    // Validation: end date must be after start date
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null checks
        }
        return endDate.isAfter(startDate);
    }

    // Validation for percent discount
    @AssertTrue(message = "Percent discount value must be between 1 and 100")
    public boolean isValidPercentDiscount() {
        if ("PERCENT".equals(discountType) && discountValue != null) {
            return discountValue.compareTo(BigDecimal.ONE) >= 0 &&
                   discountValue.compareTo(BigDecimal.valueOf(100)) <= 0;
        }
        return true;
    }
}
