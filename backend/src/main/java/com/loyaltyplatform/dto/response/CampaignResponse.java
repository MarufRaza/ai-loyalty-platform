package com.loyaltyplatform.dto.response;

import com.loyaltyplatform.enums.CampaignStatus;
import com.loyaltyplatform.enums.CampaignType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignResponse {
    private Long id;
    private String name;
    private String objective;
    private CampaignType campaignType;
    private CampaignStatus status;
    private String subjectLine;
    private String content;
    private String callToAction;
    private Long segmentId;
    private String segmentName;
    private String createdByName;
    private LocalDateTime scheduledAt;
    private LocalDateTime publishedAt;
    private LocalDateTime completedAt;
    private boolean aiGenerated;
    private CampaignAnalyticsResponse analytics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
