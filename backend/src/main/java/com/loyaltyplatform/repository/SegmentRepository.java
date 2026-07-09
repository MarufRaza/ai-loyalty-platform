package com.loyaltyplatform.repository;

import com.loyaltyplatform.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Long> {
    List<Segment> findByIsActiveTrue();
    boolean existsByName(String name);
}
