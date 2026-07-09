package com.loyaltyplatform.repository;

import com.loyaltyplatform.entity.LoyaltyTransaction;
import com.loyaltyplatform.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    Page<LoyaltyTransaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    @Query("SELECT SUM(lt.points) FROM LoyaltyTransaction lt " +
           "WHERE lt.customer.id = :customerId AND lt.transactionType = :type")
    Integer sumPointsByCustomerAndType(@Param("customerId") Long customerId,
                                       @Param("type") TransactionType type);

    @Query("SELECT SUM(lt.points) FROM LoyaltyTransaction lt " +
           "WHERE lt.transactionType = 'EARN' AND lt.createdAt >= :since")
    Long totalPointsEarnedSince(@Param("since") LocalDateTime since);
}
