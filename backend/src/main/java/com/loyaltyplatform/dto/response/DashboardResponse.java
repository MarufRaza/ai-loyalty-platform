package com.loyaltyplatform.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private long totalCustomers;
    private long activeCustomers;
    private long newCustomersThisMonth;
    private long totalCampaigns;
    private long activeCampaigns;
    private long totalCoupons;
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;

    private Map<String, Long> customersByTier;
    private Map<String, Long> campaignsByStatus;
    private Map<String, Long> campaignsByType;

    private List<MonthlyGrowth> monthlyGrowth;
    private List<TopSegment> topSegments;
    private List<RecentCampaign> recentCampaigns;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyGrowth {
        private String month;
        private long newCustomers;
        private BigDecimal revenue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopSegment {
        private String name;
        private int customerCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentCampaign {
        private Long id;
        private String name;
        private String type;
        private String status;
        private int totalSent;
    }
}
