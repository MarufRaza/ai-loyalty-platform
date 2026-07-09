package com.loyaltyplatform.dto.response;

import com.loyaltyplatform.enums.CampaignType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AICampaignResponse {
    private String subjectLine;
    private String content;
    private String callToAction;
    private CampaignType campaignType;
    private String objective;
    private String generatedBy;
    private boolean success;
    private String errorMessage;
}
