package com.loyaltyplatform.controller;

import com.loyaltyplatform.dto.request.SegmentRequest;
import com.loyaltyplatform.dto.response.ApiResponse;
import com.loyaltyplatform.dto.response.SegmentResponse;
import com.loyaltyplatform.service.SegmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/segments")
@RequiredArgsConstructor
@Tag(name = "Customer Segmentation", description = "Create and manage customer segments")
@SecurityRequirement(name = "Bearer Authentication")
public class SegmentController {

    private final SegmentService segmentService;

    @PostMapping
    @Operation(summary = "Create a customer segment")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<ApiResponse<SegmentResponse>> createSegment(
            @Valid @RequestBody SegmentRequest request) {
        SegmentResponse response = segmentService.createSegment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Segment created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all active segments")
    public ResponseEntity<ApiResponse<List<SegmentResponse>>> getAllSegments() {
        return ResponseEntity.ok(ApiResponse.success(segmentService.getAllActiveSegments()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get segment by ID")
    public ResponseEntity<ApiResponse<SegmentResponse>> getSegment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(segmentService.getSegmentById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a segment")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<ApiResponse<SegmentResponse>> updateSegment(
            @PathVariable Long id,
            @Valid @RequestBody SegmentRequest request) {
        SegmentResponse response = segmentService.updateSegment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Segment updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a segment")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteSegment(@PathVariable Long id) {
        segmentService.deleteSegment(id);
        return ResponseEntity.ok(ApiResponse.success("Segment deactivated", null));
    }
}
