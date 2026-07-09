package com.loyaltyplatform.controller;

import com.loyaltyplatform.dto.request.AICampaignRequest;
import com.loyaltyplatform.dto.response.AICampaignResponse;
import com.loyaltyplatform.dto.response.ApiResponse;
import com.loyaltyplatform.service.AICampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/campaigns")
@RequiredArgsConstructor
@Tag(name = "AI Campaign Generator", description = "Generate marketing campaigns using the Groq AI API")
@SecurityRequirement(name = "Bearer Authentication")
public class AICampaignController {

    private final AICampaignService aiCampaignService;

    @PostMapping("/generate")
    @Operation(summary = "Generate an AI-powered marketing campaign",
               description = "Uses the Groq API with Llama 3.3 to generate campaign content")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<ApiResponse<AICampaignResponse>> generateCampaign(
            @Valid @RequestBody AICampaignRequest request) {
        AICampaignResponse response = aiCampaignService.generateCampaign(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(
                    ApiResponse.success("Campaign generated successfully", response));
        }
        return ResponseEntity.ok(
                ApiResponse.error(response.getErrorMessage(), "AI_GENERATION_FAILED"));
    }
}
