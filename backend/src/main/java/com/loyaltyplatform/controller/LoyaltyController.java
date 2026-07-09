package com.loyaltyplatform.controller;

import com.loyaltyplatform.dto.request.LoyaltyTransactionRequest;
import com.loyaltyplatform.dto.response.ApiResponse;
import com.loyaltyplatform.dto.response.LoyaltyTransactionResponse;
import com.loyaltyplatform.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty Program", description = "Points earning, redemption and transaction history")
@SecurityRequirement(name = "Bearer Authentication")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @PostMapping("/transactions")
    @Operation(summary = "Process a loyalty transaction (earn/redeem points)")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> processTransaction(
            @Valid @RequestBody LoyaltyTransactionRequest request) {
        LoyaltyTransactionResponse response = loyaltyService.processTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction processed successfully", response));
    }

    @GetMapping("/customers/{customerId}/transactions")
    @Operation(summary = "Get loyalty transaction history for a customer")
    public ResponseEntity<ApiResponse<Page<LoyaltyTransactionResponse>>> getCustomerTransactions(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<LoyaltyTransactionResponse> transactions =
                loyaltyService.getCustomerTransactions(customerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}
