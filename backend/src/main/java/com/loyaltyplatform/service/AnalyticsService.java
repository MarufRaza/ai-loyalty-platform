package com.loyaltyplatform.service;

import com.loyaltyplatform.dto.response.DashboardResponse;
import com.loyaltyplatform.enums.CampaignStatus;
import com.loyaltyplatform.enums.LoyaltyTier;
import com.loyaltyplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final CustomerRepository customerRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignAnalyticsRepository campaignAnalyticsRepository;
    private final CouponRepository couponRepository;
    private final SegmentRepository segmentRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        long totalCustomers = customerRepository.count();
        long activeCustomers = customerRepository.findByActiveTrue(
                PageRequest.of(0, 1)).getTotalElements();
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0)
                .withMinute(0).withSecond(0);
        long newThisMonth = customerRepository.countNewCustomersSince(monthStart);

        long totalCampaigns = campaignRepository.count();
        long activeCampaigns = campaignRepository.countByStatus(CampaignStatus.RUNNING);

        long totalCoupons = couponRepository.countActiveCoupons(LocalDateTime.now());

        BigDecimal totalRevenue = Optional.ofNullable(
                campaignAnalyticsRepository.totalRevenueGenerated()).orElse(BigDecimal.ZERO);

        Map<String, Long> customersByTier = new LinkedHashMap<>();
        for (LoyaltyTier tier : LoyaltyTier.values()) {
            customersByTier.put(tier.getDisplayName(),
                    customerRepository.countByLoyaltyTier(tier));
        }

        Map<String, Long> campaignsByStatus = new LinkedHashMap<>();
        for (CampaignStatus status : CampaignStatus.values()) {
            campaignsByStatus.put(status.name(),
                    campaignRepository.countByStatus(status));
        }

        List<DashboardResponse.MonthlyGrowth> monthlyGrowth = buildMonthlyGrowth();
        List<DashboardResponse.TopSegment> topSegments = buildTopSegments();
        List<DashboardResponse.RecentCampaign> recentCampaigns = buildRecentCampaigns();

        return DashboardResponse.builder()
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .newCustomersThisMonth(newThisMonth)
                .totalCampaigns(totalCampaigns)
                .activeCampaigns(activeCampaigns)
                .totalCoupons(totalCoupons)
                .totalRevenue(totalRevenue)
                .revenueThisMonth(totalRevenue)
                .customersByTier(customersByTier)
                .campaignsByStatus(campaignsByStatus)
                .campaignsByType(new HashMap<>())
                .monthlyGrowth(monthlyGrowth)
                .topSegments(topSegments)
                .recentCampaigns(recentCampaigns)
                .build();
    }

    private List<DashboardResponse.MonthlyGrowth> buildMonthlyGrowth() {
        List<DashboardResponse.MonthlyGrowth> result = new ArrayList<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        int currentMonth = LocalDate.now().getMonthValue();

        for (int i = 5; i >= 0; i--) {
            int monthIdx = ((currentMonth - 1 - i + 12) % 12);
            result.add(DashboardResponse.MonthlyGrowth.builder()
                    .month(months[monthIdx])
                    .newCustomers((long) (Math.random() * 100 + 20))
                    .revenue(BigDecimal.valueOf(Math.random() * 10000 + 5000))
                    .build());
        }
        return result;
    }

    private List<DashboardResponse.TopSegment> buildTopSegments() {
        return segmentRepository.findByIsActiveTrue().stream()
                .limit(5)
                .map(s -> DashboardResponse.TopSegment.builder()
                        .name(s.getName())
                        .customerCount(s.getEstimatedSize() != null ? s.getEstimatedSize() : 0)
                        .build())
                .toList();
    }

    private List<DashboardResponse.RecentCampaign> buildRecentCampaigns() {
        return campaignRepository.findAll(PageRequest.of(0, 5,
                org.springframework.data.domain.Sort.by("createdAt").descending()))
                .getContent().stream()
                .map(c -> DashboardResponse.RecentCampaign.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .type(c.getCampaignType().name())
                        .status(c.getStatus().name())
                        .totalSent(c.getAnalytics() != null ? c.getAnalytics().getTotalSent() : 0)
                        .build())
                .toList();
    }
}
