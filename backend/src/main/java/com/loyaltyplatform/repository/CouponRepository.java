package com.loyaltyplatform.repository;

import com.loyaltyplatform.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    Page<Coupon> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
           "AND c.expiryDate > :now AND c.usageCount < c.usageLimit")
    Page<Coupon> findValidCoupons(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.isActive = true AND c.expiryDate > :now")
    long countActiveCoupons(@Param("now") LocalDateTime now);
}
