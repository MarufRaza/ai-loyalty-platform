package com.loyaltyplatform.service;

import com.loyaltyplatform.dto.request.AICampaignRequest;
import com.loyaltyplatform.dto.response.AICampaignResponse;
import com.loyaltyplatform.enums.CampaignType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AICampaignService {

    private final WebClient aiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${ai.timeout:30}")
    private int timeoutSeconds;

    public AICampaignService(@Qualifier("aiWebClient") WebClient aiWebClient,
                              ObjectMapper objectMapper) {
        this.aiWebClient = aiWebClient;
        this.objectMapper = objectMapper;
    }

    public AICampaignResponse generateCampaign(AICampaignRequest request) {
        String prompt = buildPrompt(request);

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "temperature", 0.7,
                    "max_tokens", 600
            );

            String responseBody = aiWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .onErrorResume(ex -> {
                        log.error("AI API error: {}", ex.getMessage());
                        return Mono.just("{\"choices\":[{\"message\":{\"content\":\"AI service temporarily unavailable\"}}]}");
                    })
                    .block();

            return parseAIResponse(responseBody, request.getCampaignType(), request.getObjective());

        } catch (Exception e) {
            log.error("Error generating AI campaign: {}", e.getMessage(), e);
            return AICampaignResponse.builder()
                    .success(false)
                    .errorMessage("Failed to generate campaign: " + e.getMessage())
                    .campaignType(request.getCampaignType())
                    .objective(request.getObjective())
                    .build();
        }
    }

    private String buildPrompt(AICampaignRequest request) {
        String channelContext = switch (request.getCampaignType()) {
            case EMAIL -> "email newsletter";
            case WHATSAPP -> "WhatsApp message (keep it short, under 200 words)";
            case SMS -> "SMS message (keep it very brief, under 160 characters)";
            case PUSH_NOTIFICATION -> "push notification (very brief, under 50 characters for title, 100 for body)";
        };

        String tone = request.getTone() != null ? request.getTone() : "professional and friendly";
        String brand = request.getBrandName() != null ? request.getBrandName() : "LoyaltyPro";
        String audience = request.getTargetAudience() != null ?
                request.getTargetAudience() : "loyalty program members";

        return String.format("""
                You are a marketing copywriter for %s, a customer loyalty platform.
                
                Create a compelling %s campaign with the following details:
                - Campaign Objective: %s
                - Target Audience: %s
                - Segment: %s
                - Tone: %s
                
                Please provide the output in this EXACT format:
                SUBJECT: [compelling subject line or title]
                CONTENT: [main marketing message]
                CTA: [clear call-to-action button text]
                
                Keep it concise, engaging and action-oriented. Focus on value for the customer.
                """,
                brand,
                channelContext,
                request.getObjective(),
                audience,
                request.getSegmentName() != null ? request.getSegmentName() : "All customers",
                tone
        );
    }

    private AICampaignResponse parseAIResponse(String responseBody,
                                                CampaignType campaignType,
                                                String objective) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            // OpenAI/Groq chat completions format
            String rawText = root.path("choices").get(0)
                    .path("message").path("content").asText();
            if (rawText == null || rawText.isBlank()) {
                // Fallback: try Ollama legacy format
                rawText = root.path("response").asText();
            }

            String subject = extractSection(rawText, "SUBJECT:");
            String content = extractSection(rawText, "CONTENT:");
            String cta = extractSection(rawText, "CTA:");

            if (subject.isEmpty() && content.isEmpty()) {
                subject = "Special Offer Just For You!";
                content = rawText.trim();
                cta = "Shop Now";
            }

            return AICampaignResponse.builder()
                    .subjectLine(subject.isEmpty() ? "Exclusive Offer Inside" : subject)
                    .content(content.isEmpty() ? rawText : content)
                    .callToAction(cta.isEmpty() ? "Learn More" : cta)
                    .campaignType(campaignType)
                    .objective(objective)
                    .generatedBy(model)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing Ollama response: {}", e.getMessage());
            return AICampaignResponse.builder()
                    .success(false)
                    .errorMessage("Failed to parse AI response")
                    .campaignType(campaignType)
                    .objective(objective)
                    .build();
        }
    }

    private String extractSection(String text, String label) {
        int start = text.indexOf(label);
        if (start == -1) return "";
        start += label.length();
        int end = findNextSection(text, start);
        return text.substring(start, end).trim();
    }

    private int findNextSection(String text, int start) {
        String[] labels = {"SUBJECT:", "CONTENT:", "CTA:"};
        int next = text.length();
        for (String label : labels) {
            int pos = text.indexOf(label, start);
            if (pos != -1 && pos < next) {
                next = pos;
            }
        }
        return next;
    }
}
