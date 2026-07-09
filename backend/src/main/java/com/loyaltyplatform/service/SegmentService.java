package com.loyaltyplatform.service;

import com.loyaltyplatform.dto.request.SegmentRequest;
import com.loyaltyplatform.dto.response.SegmentResponse;
import com.loyaltyplatform.entity.Segment;
import com.loyaltyplatform.entity.User;
import com.loyaltyplatform.exception.DuplicateResourceException;
import com.loyaltyplatform.exception.ResourceNotFoundException;
import com.loyaltyplatform.repository.SegmentRepository;
import com.loyaltyplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SegmentService {

    private final SegmentRepository segmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public SegmentResponse createSegment(SegmentRequest request) {
        if (segmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Segment already exists: " + request.getName());
        }

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByEmail(currentEmail).orElse(null);

        Segment segment = Segment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .filterCriteria(request.getFilterCriteria())
                .createdBy(creator)
                .isActive(true)
                .build();

        return mapToResponse(segmentRepository.save(segment));
    }

    @Transactional(readOnly = true)
    public List<SegmentResponse> getAllActiveSegments() {
        return segmentRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public SegmentResponse getSegmentById(Long id) {
        return mapToResponse(findById(id));
    }

    @Transactional
    public SegmentResponse updateSegment(Long id, SegmentRequest request) {
        Segment segment = findById(id);
        segment.setName(request.getName());
        segment.setDescription(request.getDescription());
        segment.setFilterCriteria(request.getFilterCriteria());
        return mapToResponse(segmentRepository.save(segment));
    }

    @Transactional
    public void deleteSegment(Long id) {
        Segment segment = findById(id);
        segment.setActive(false);
        segmentRepository.save(segment);
    }

    private Segment findById(Long id) {
        return segmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Segment", "id", id));
    }

    public SegmentResponse mapToResponse(Segment segment) {
        return SegmentResponse.builder()
                .id(segment.getId())
                .name(segment.getName())
                .description(segment.getDescription())
                .filterCriteria(segment.getFilterCriteria())
                .isActive(segment.isActive())
                .estimatedSize(segment.getEstimatedSize())
                .createdByName(segment.getCreatedBy() != null ?
                        segment.getCreatedBy().getName() : null)
                .createdAt(segment.getCreatedAt())
                .updatedAt(segment.getUpdatedAt())
                .build();
    }
}
