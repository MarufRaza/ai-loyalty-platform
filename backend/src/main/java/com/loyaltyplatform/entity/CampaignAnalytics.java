package com.loyaltyplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_analytics",
        indexes = @Index(name = "idx_analytics_campaign", columnList = "campaign_id"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalSent = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalOpened = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalClicked = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalConverted = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalBounced = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalUnsubscribed = 0;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal openRate = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal clickRate = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal conversionRate = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal revenueGenerated = BigDecimal.ZERO;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
