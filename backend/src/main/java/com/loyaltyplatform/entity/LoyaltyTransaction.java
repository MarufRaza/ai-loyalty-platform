package com.loyaltyplatform.entity;

import com.loyaltyplatform.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_transactions",
        indexes = {
                @Index(name = "idx_loyalty_tx_customer", columnList = "customer_id"),
                @Index(name = "idx_loyalty_tx_type", columnList = "transaction_type"),
                @Index(name = "idx_loyalty_tx_created", columnList = "created_at")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer points;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String referenceId;

    @Column(nullable = false)
    private Integer balanceAfter;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
