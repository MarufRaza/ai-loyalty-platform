package com.loyaltyplatform.dto.request;

import com.loyaltyplatform.enums.CouponType;
import com.loyaltyplatform.enums.LoyaltyTier;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRequest {

    @NotBlank(message = "Description is required")
    @Size(max = 200)
    private String description;

    @NotNull(message = "Coupon type is required")
    private CouponType couponType;

    @DecimalMin(value = "0.01")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal discountValue;

    @DecimalMin(value = "0.01")
    @DecimalMax(value = "100.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal discountPercentage;

    @DecimalMin(value = "0.0")
    private BigDecimal minimumOrderAmount;

    @DecimalMin(value = "0.0")
    private BigDecimal maximumDiscountAmount;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDateTime expiryDate;

    @Min(1)
    @Builder.Default
    private Integer usageLimit = 1;

    private LoyaltyTier minimumTierRequired;
}
