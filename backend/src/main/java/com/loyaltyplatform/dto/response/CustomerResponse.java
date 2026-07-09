package com.loyaltyplatform.dto.response;

import com.loyaltyplatform.enums.LoyaltyTier;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String city;
    private BigDecimal lifetimeValue;
    private Integer loyaltyPoints;
    private LoyaltyTier loyaltyTier;
    private String loyaltyTierDisplayName;
    private LocalDate lastPurchaseDate;
    private String notes;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private double nextTierProgress;
    private Integer pointsToNextTier;
}
