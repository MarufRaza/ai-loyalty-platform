package com.loyaltyplatform.dto.request;

import com.loyaltyplatform.enums.CampaignType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AICampaignRequest {

    @NotNull(message = "Campaign type is required")
    private CampaignType campaignType;

    @NotBlank(message = "Campaign objective is required")
    private String objective;

    private Long segmentId;
    private String segmentName;
    private String targetAudience;
    private String tone;
    private String brandName;
}
