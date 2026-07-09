package com.loyaltyplatform.service;

import com.loyaltyplatform.dto.request.CampaignRequest;
import com.loyaltyplatform.dto.response.CampaignAnalyticsResponse;
import com.loyaltyplatform.dto.response.CampaignResponse;
import com.loyaltyplatform.entity.*;
import com.loyaltyplatform.enums.AuditAction;
import com.loyaltyplatform.enums.CampaignStatus;
import com.loyaltyplatform.exception.BadRequestException;
import com.loyaltyplatform.exception.ResourceNotFoundException;
import com.loyaltyplatform.repository.*;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignAnalyticsRepository analyticsRepository;
    private final SegmentRepository segmentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final Counter campaignCreatedCounter;

    @Transactional
    public CampaignResponse createCampaign(CampaignRequest request) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByEmail(currentEmail).orElse(null);

        Segment segment = null;
        if (request.getSegmentId() != null) {
            segment = segmentRepository.findById(request.getSegmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Segment", "id",
                            request.getSegmentId()));
        }

        Campaign campaign = Campaign.builder()
                .name(request.getName())
                .objective(request.getObjective())
                .campaignType(request.getCampaignType())
                .status(request.getStatus() != null ? request.getStatus() : CampaignStatus.DRAFT)
                .subjectLine(request.getSubjectLine())
                .content(request.getContent())
                .callToAction(request.getCallToAction())
                .segment(segment)
                .createdBy(creator)
                .scheduledAt(request.getScheduledAt())
                .aiGenerated(request.isAiGenerated())
                .build();

        Campaign saved = campaignRepository.save(campaign);
        campaignCreatedCounter.increment();

        CampaignAnalytics analytics = CampaignAnalytics.builder()
                .campaign(saved)
                .build();
        analyticsRepository.save(analytics);

        auditLogService.logByEmail(currentEmail, AuditAction.CAMPAIGN_CREATED,
                "Campaign", saved.getId(), "Campaign created: " + saved.getName());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> getAllCampaigns(CampaignStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Campaign> campaigns = status != null ?
                campaignRepository.findByStatus(status, pageable) :
                campaignRepository.findAll(pageable);
        return campaigns.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public CampaignResponse getCampaignById(Long id) {
        return mapToResponse(findById(id));
    }

    @Transactional
    public CampaignResponse publishCampaign(Long id) {
        Campaign campaign = findById(id);

        if (campaign.getStatus() != CampaignStatus.DRAFT &&
                campaign.getStatus() != CampaignStatus.SCHEDULED) {
            throw new BadRequestException(
                    "Cannot publish campaign with status: " + campaign.getStatus());
        }

        campaign.setStatus(CampaignStatus.RUNNING);
        campaign.setPublishedAt(LocalDateTime.now());
        Campaign updated = campaignRepository.save(campaign);

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.logByEmail(currentUser, AuditAction.CAMPAIGN_PUBLISHED,
                "Campaign", id, "Campaign published: " + campaign.getName());

        return mapToResponse(updated);
    }

    @Transactional
    public CampaignResponse cancelCampaign(Long id) {
        Campaign campaign = findById(id);

        if (campaign.getStatus() == CampaignStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed campaign");
        }

        campaign.setStatus(CampaignStatus.CANCELLED);
        Campaign updated = campaignRepository.save(campaign);

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.logByEmail(currentUser, AuditAction.CAMPAIGN_CANCELLED,
                "Campaign", id, "Campaign cancelled: " + campaign.getName());

        return mapToResponse(updated);
    }

    @Transactional
    public CampaignResponse updateCampaign(Long id, CampaignRequest request) {
        Campaign campaign = findById(id);

        if (campaign.getStatus() != CampaignStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT campaigns can be edited");
        }

        Segment segment = null;
        if (request.getSegmentId() != null) {
            segment = segmentRepository.findById(request.getSegmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Segment", "id",
                            request.getSegmentId()));
        }

        campaign.setName(request.getName());
        campaign.setObjective(request.getObjective());
        campaign.setCampaignType(request.getCampaignType());
        campaign.setSubjectLine(request.getSubjectLine());
        campaign.setContent(request.getContent());
        campaign.setCallToAction(request.getCallToAction());
        campaign.setSegment(segment);
        campaign.setScheduledAt(request.getScheduledAt());

        return mapToResponse(campaignRepository.save(campaign));
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processScheduledCampaigns() {
        List<Campaign> due = campaignRepository.findDueCampaigns(LocalDateTime.now());
        for (Campaign campaign : due) {
            campaign.setStatus(CampaignStatus.RUNNING);
            campaign.setPublishedAt(LocalDateTime.now());
            campaignRepository.save(campaign);
            log.info("Auto-published scheduled campaign: {}", campaign.getName());
        }
    }

    private Campaign findById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", "id", id));
    }

    public CampaignResponse mapToResponse(Campaign campaign) {
        CampaignAnalyticsResponse analyticsResponse = null;
        if (campaign.getAnalytics() != null) {
            CampaignAnalytics a = campaign.getAnalytics();
            analyticsResponse = CampaignAnalyticsResponse.builder()
                    .id(a.getId())
                    .totalSent(a.getTotalSent())
                    .totalOpened(a.getTotalOpened())
                    .totalClicked(a.getTotalClicked())
                    .totalConverted(a.getTotalConverted())
                    .totalBounced(a.getTotalBounced())
                    .totalUnsubscribed(a.getTotalUnsubscribed())
                    .openRate(a.getOpenRate())
                    .clickRate(a.getClickRate())
                    .conversionRate(a.getConversionRate())
                    .revenueGenerated(a.getRevenueGenerated())
                    .build();
        }

        return CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .objective(campaign.getObjective())
                .campaignType(campaign.getCampaignType())
                .status(campaign.getStatus())
                .subjectLine(campaign.getSubjectLine())
                .content(campaign.getContent())
                .callToAction(campaign.getCallToAction())
                .segmentId(campaign.getSegment() != null ? campaign.getSegment().getId() : null)
                .segmentName(campaign.getSegment() != null ? campaign.getSegment().getName() : null)
                .createdByName(campaign.getCreatedBy() != null ?
                        campaign.getCreatedBy().getName() : null)
                .scheduledAt(campaign.getScheduledAt())
                .publishedAt(campaign.getPublishedAt())
                .completedAt(campaign.getCompletedAt())
                .aiGenerated(campaign.isAiGenerated())
                .analytics(analyticsResponse)
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .build();
    }
}
