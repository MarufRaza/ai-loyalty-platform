package com.loyaltyplatform.controller;

import com.loyaltyplatform.dto.request.CouponRequest;
import com.loyaltyplatform.dto.response.ApiResponse;
import com.loyaltyplatform.dto.response.CouponResponse;
import com.loyaltyplatform.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon Engine", description = "Generate, manage and redeem coupons")
@SecurityRequirement(name = "Bearer Authentication")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "Create a new coupon")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all coupons with pagination")
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getAllCoupons(page, size)));
    }

    @GetMapping("/valid")
    @Operation(summary = "Get all currently valid coupons")
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getValidCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getValidCoupons(page, size)));
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get coupon by code")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCouponByCode(code)));
    }

    @PostMapping("/{code}/redeem")
    @Operation(summary = "Redeem a coupon for a customer")
    public ResponseEntity<ApiResponse<CouponResponse>> redeemCoupon(
            @PathVariable String code,
            @RequestParam Long customerId,
            @RequestParam BigDecimal orderAmount) {
        CouponResponse response = couponService.redeemCoupon(code, customerId, orderAmount);
        return ResponseEntity.ok(ApiResponse.success("Coupon redeemed successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a coupon")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deactivateCoupon(@PathVariable Long id) {
        couponService.deactivateCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deactivated", null));
    }
}
