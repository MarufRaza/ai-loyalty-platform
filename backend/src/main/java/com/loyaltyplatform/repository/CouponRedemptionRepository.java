package com.loyaltyplatform.repository;

import com.loyaltyplatform.entity.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    boolean existsByCouponIdAndCustomerId(Long couponId, Long customerId);

    @Query("SELECT COUNT(cr) FROM CouponRedemption cr WHERE cr.coupon.id = :couponId")
    int countRedemptionsByCoupon(@Param("couponId") Long couponId);
}
