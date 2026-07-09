package com.loyaltyplatform.entity;

import com.loyaltyplatform.enums.CampaignStatus;
import com.loyaltyplatform.enums.CampaignType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns",
        indexes = {
                @Index(name = "idx_campaigns_status", columnList = "status"),
                @Index(name = "idx_campaigns_type", columnList = "campaign_type"),
                @Index(name = "idx_campaigns_scheduled", columnList = "scheduled_at")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String objective;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CampaignType campaignType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(length = 300)
    private String subjectLine;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 200)
    private String callToAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id")
    private Segment segment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column
    private LocalDateTime scheduledAt;

    @Column
    private LocalDateTime publishedAt;

    @Column
    private LocalDateTime completedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean aiGenerated = false;

    @OneToOne(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CampaignAnalytics analytics;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
