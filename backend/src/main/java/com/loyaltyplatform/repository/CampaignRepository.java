package com.loyaltyplatform.repository;

import com.loyaltyplatform.entity.Campaign;
import com.loyaltyplatform.enums.CampaignStatus;
import com.loyaltyplatform.enums.CampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Page<Campaign> findByStatus(CampaignStatus status, Pageable pageable);

    Page<Campaign> findByCampaignType(CampaignType type, Pageable pageable);

    @Query("SELECT c FROM Campaign c WHERE c.status = 'SCHEDULED' " +
           "AND c.scheduledAt <= :now ORDER BY c.scheduledAt ASC")
    List<Campaign> findDueCampaigns(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.status = :status")
    long countByStatus(@Param("status") CampaignStatus status);

    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.createdAt >= :since")
    long countCreatedSince(@Param("since") LocalDateTime since);
}
