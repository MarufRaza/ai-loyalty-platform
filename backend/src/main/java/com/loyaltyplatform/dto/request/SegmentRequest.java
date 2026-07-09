package com.loyaltyplatform.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentRequest {

    @NotBlank(message = "Segment name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotBlank(message = "Filter criteria is required")
    private String filterCriteria;
}
