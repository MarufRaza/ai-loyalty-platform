package com.loyaltyplatform.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MetricsConfig {

    private final AtomicInteger activeCampaigns = new AtomicInteger(0);

    @Bean
    public Counter customerCreatedCounter(MeterRegistry registry) {
        return Counter.builder("loyalty.customers.created")
                .description("Total customers created")
                .register(registry);
    }

    @Bean
    public Counter campaignCreatedCounter(MeterRegistry registry) {
        return Counter.builder("loyalty.campaigns.created")
                .description("Total campaigns created")
                .register(registry);
    }

    @Bean
    public Counter pointsEarnedCounter(MeterRegistry registry) {
        return Counter.builder("loyalty.points.earned")
                .description("Total loyalty points earned")
                .register(registry);
    }

    @Bean
    public Counter couponRedeemedCounter(MeterRegistry registry) {
        return Counter.builder("loyalty.coupons.redeemed")
                .description("Total coupons redeemed")
                .register(registry);
    }

    @Bean
    public Gauge activeCampaignsGauge(MeterRegistry registry) {
        return Gauge.builder("loyalty.campaigns.active", activeCampaigns, AtomicInteger::get)
                .description("Currently active campaigns")
                .register(registry);
    }

    public AtomicInteger getActiveCampaigns() {
        return activeCampaigns;
    }
}
