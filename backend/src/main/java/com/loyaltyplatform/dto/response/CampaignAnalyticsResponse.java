package com.loyaltyplatform.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignAnalyticsResponse {
    private Long id;
    private Integer totalSent;
    private Integer totalOpened;
    private Integer totalClicked;
    private Integer totalConverted;
    private Integer totalBounced;
    private Integer totalUnsubscribed;
    private BigDecimal openRate;
    private BigDecimal clickRate;
    private BigDecimal conversionRate;
    private BigDecimal revenueGenerated;
}
