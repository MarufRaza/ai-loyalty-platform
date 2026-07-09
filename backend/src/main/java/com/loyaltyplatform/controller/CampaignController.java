package com.loyaltyplatform.controller;

import com.loyaltyplatform.dto.request.CampaignRequest;
import com.loyaltyplatform.dto.response.ApiResponse;
import com.loyaltyplatform.dto.response.CampaignResponse;
import com.loyaltyplatform.enums.CampaignStatus;
import com.loyaltyplatform.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaign Management", description = "Create, schedule, publish and track campaigns")
@SecurityRequirement(name = "Bearer Authentication")
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @Operation(summary = "Create a new campaign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(
            @Valid @RequestBody CampaignRequest request) {
        CampaignResponse response = campaignService.createCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Campaign created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all campaigns with optional status filter")
    public ResponseEntity<ApiResponse<Page<CampaignResponse>>> getAllCampaigns(
            @RequestParam(required = false) CampaignStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(campaignService.getAllCampaigns(status, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get campaign by ID")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.getCampaignById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a draft campaign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Campaign updated", campaignService.updateCampaign(id, request)));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a campaign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> publishCampaign(@PathVariable Long id) {
        CampaignResponse response = campaignService.publishCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign published", response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a campaign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> cancelCampaign(@PathVariable Long id) {
        CampaignResponse response = campaignService.cancelCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign cancelled", response));
    }
}
