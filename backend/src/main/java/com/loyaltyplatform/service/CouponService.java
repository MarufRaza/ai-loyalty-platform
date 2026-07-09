package com.loyaltyplatform.service;

import com.loyaltyplatform.dto.request.CouponRequest;
import com.loyaltyplatform.dto.response.CouponResponse;
import com.loyaltyplatform.entity.*;
import com.loyaltyplatform.enums.AuditAction;
import com.loyaltyplatform.exception.BadRequestException;
import com.loyaltyplatform.exception.ResourceNotFoundException;
import com.loyaltyplatform.repository.*;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository redemptionRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final Counter couponRedeemedCounter;

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        String code = generateUniqueCode();

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByEmail(currentEmail).orElse(null);

        Coupon coupon = Coupon.builder()
                .code(code)
                .description(request.getDescription())
                .couponType(request.getCouponType())
                .discountValue(request.getDiscountValue())
                .discountPercentage(request.getDiscountPercentage())
                .minimumOrderAmount(request.getMinimumOrderAmount())
                .maximumDiscountAmount(request.getMaximumDiscountAmount())
                .expiryDate(request.getExpiryDate())
                .usageLimit(request.getUsageLimit())
                .minimumTierRequired(request.getMinimumTierRequired())
                .createdBy(creator)
                .isActive(true)
                .usageCount(0)
                .build();

        Coupon saved = couponRepository.save(coupon);

        auditLogService.logByEmail(currentEmail, AuditAction.COUPON_CREATED,
                "Coupon", saved.getId(), "Coupon created: " + code);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<CouponResponse> getAllCoupons(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return couponRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<CouponResponse> getValidCoupons(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());
        return couponRepository.findValidCoupons(LocalDateTime.now(), pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));
        return mapToResponse(coupon);
    }

    @Transactional
    public CouponResponse redeemCoupon(String code, Long customerId, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        if (!coupon.isActive()) {
            throw new BadRequestException("Coupon is not active");
        }
        if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Coupon has expired");
        }
        if (coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Coupon usage limit exceeded");
        }
        if (redemptionRepository.existsByCouponIdAndCustomerId(coupon.getId(), customerId)) {
            throw new BadRequestException("Coupon already redeemed by this customer");
        }
        if (coupon.getMinimumOrderAmount() != null &&
                orderAmount.compareTo(coupon.getMinimumOrderAmount()) < 0) {
            throw new BadRequestException(
                    "Minimum order amount not met. Required: " + coupon.getMinimumOrderAmount());
        }
        if (coupon.getMinimumTierRequired() != null &&
                customer.getLoyaltyTier().ordinal() < coupon.getMinimumTierRequired().ordinal()) {
            throw new BadRequestException(
                    "Customer tier does not meet minimum requirement: " +
                    coupon.getMinimumTierRequired().getDisplayName());
        }

        BigDecimal discount = calculateDiscount(coupon, orderAmount);

        CouponRedemption redemption = CouponRedemption.builder()
                .coupon(coupon)
                .customer(customer)
                .discountApplied(discount)
                .orderAmount(orderAmount)
                .build();

        redemptionRepository.save(redemption);
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        Coupon updated = couponRepository.save(coupon);

        couponRedeemedCounter.increment();

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.logByEmail(currentUser, AuditAction.COUPON_REDEEMED,
                "Coupon", coupon.getId(),
                "Coupon " + code + " redeemed by customer " + customer.getEmail());

        return mapToResponse(updated);
    }

    @Transactional
    public void deactivateCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        coupon.setActive(false);
        couponRepository.save(coupon);
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        return switch (coupon.getCouponType()) {
            case FLAT_DISCOUNT -> coupon.getDiscountValue() != null ?
                    coupon.getDiscountValue() : BigDecimal.ZERO;
            case PERCENTAGE_DISCOUNT -> {
                BigDecimal discount = orderAmount.multiply(
                        coupon.getDiscountPercentage().divide(BigDecimal.valueOf(100)));
                if (coupon.getMaximumDiscountAmount() != null) {
                    yield discount.min(coupon.getMaximumDiscountAmount());
                }
                yield discount;
            }
            default -> BigDecimal.ZERO;
        };
    }

    private String generateUniqueCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder(10);
            for (int i = 0; i < 10; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (couponRepository.existsByCode(code));
        return code;
    }

    public CouponResponse mapToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .couponType(coupon.getCouponType())
                .discountValue(coupon.getDiscountValue())
                .discountPercentage(coupon.getDiscountPercentage())
                .minimumOrderAmount(coupon.getMinimumOrderAmount())
                .maximumDiscountAmount(coupon.getMaximumDiscountAmount())
                .expiryDate(coupon.getExpiryDate())
                .active(coupon.isActive())
                .usageLimit(coupon.getUsageLimit())
                .usageCount(coupon.getUsageCount())
                .remainingUses(coupon.getUsageLimit() - coupon.getUsageCount())
                .minimumTierRequired(coupon.getMinimumTierRequired())
                .createdByName(coupon.getCreatedBy() != null ? coupon.getCreatedBy().getName() : null)
                .createdAt(coupon.getCreatedAt())
                .expired(coupon.getExpiryDate().isBefore(LocalDateTime.now()))
                .build();
    }
}
