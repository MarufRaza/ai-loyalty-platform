package com.loyaltyplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_redemptions",
        indexes = {
                @Index(name = "idx_redemption_coupon", columnList = "coupon_id"),
                @Index(name = "idx_redemption_customer", columnList = "customer_id")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountApplied;

    @Column(precision = 10, scale = 2)
    private BigDecimal orderAmount;

    @Column(length = 100)
    private String orderId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime redeemedAt;
}
