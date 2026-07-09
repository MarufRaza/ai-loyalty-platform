package com.loyaltyplatform.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentResponse {
    private Long id;
    private String name;
    private String description;
    private String filterCriteria;
    private boolean isActive;
    private Integer estimatedSize;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
