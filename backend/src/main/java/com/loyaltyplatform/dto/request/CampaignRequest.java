package com.loyaltyplatform.dto.request;

import com.loyaltyplatform.enums.CampaignStatus;
import com.loyaltyplatform.enums.CampaignType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignRequest {

    @NotBlank(message = "Campaign name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Campaign objective is required")
    @Size(max = 500)
    private String objective;

    @NotNull(message = "Campaign type is required")
    private CampaignType campaignType;

    @Size(max = 300)
    private String subjectLine;

    @NotBlank(message = "Campaign content is required")
    private String content;

    @Size(max = 200)
    private String callToAction;

    private Long segmentId;

    private LocalDateTime scheduledAt;

    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Builder.Default
    private boolean aiGenerated = false;
}
