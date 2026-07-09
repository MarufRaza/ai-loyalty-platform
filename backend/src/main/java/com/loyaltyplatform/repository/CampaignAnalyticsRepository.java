package com.loyaltyplatform.repository;

import com.loyaltyplatform.entity.CampaignAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface CampaignAnalyticsRepository extends JpaRepository<CampaignAnalytics, Long> {

    Optional<CampaignAnalytics> findByCampaignId(Long campaignId);

    @Query("SELECT SUM(ca.revenueGenerated) FROM CampaignAnalytics ca")
    BigDecimal totalRevenueGenerated();

    @Query("SELECT SUM(ca.totalConverted) FROM CampaignAnalytics ca")
    Long totalConversions();
}
