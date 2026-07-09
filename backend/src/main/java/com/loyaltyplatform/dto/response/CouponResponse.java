package com.loyaltyplatform.dto.response;

import com.loyaltyplatform.enums.CouponType;
import com.loyaltyplatform.enums.LoyaltyTier;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    private Long id;
    private String code;
    private String description;
    private CouponType couponType;
    private BigDecimal discountValue;
    private BigDecimal discountPercentage;
    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscountAmount;
    private LocalDateTime expiryDate;
    private boolean active;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer remainingUses;
    private LoyaltyTier minimumTierRequired;
    private String createdByName;
    private LocalDateTime createdAt;
    private boolean expired;
}
