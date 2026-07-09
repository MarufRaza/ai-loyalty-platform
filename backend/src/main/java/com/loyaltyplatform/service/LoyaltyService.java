package com.loyaltyplatform.service;

import com.loyaltyplatform.dto.request.LoyaltyTransactionRequest;
import com.loyaltyplatform.dto.response.LoyaltyTransactionResponse;
import com.loyaltyplatform.entity.Customer;
import com.loyaltyplatform.entity.LoyaltyTransaction;
import com.loyaltyplatform.enums.AuditAction;
import com.loyaltyplatform.enums.LoyaltyTier;
import com.loyaltyplatform.enums.TransactionType;
import com.loyaltyplatform.exception.BadRequestException;
import com.loyaltyplatform.repository.LoyaltyTransactionRepository;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private final LoyaltyTransactionRepository transactionRepository;
    private final CustomerService customerService;
    private final AuditLogService auditLogService;
    private final Counter pointsEarnedCounter;

    @Value("${app.loyalty.points-per-dollar:10}")
    private int pointsPerDollar;

    @Transactional
    public LoyaltyTransactionResponse processTransaction(LoyaltyTransactionRequest request) {
        Customer customer = customerService.findById(request.getCustomerId());
        int currentPoints = customer.getLoyaltyPoints();

        if (request.getTransactionType() == TransactionType.REDEEM) {
            if (currentPoints < request.getPoints()) {
                throw new BadRequestException(
                        "Insufficient points. Available: " + currentPoints +
                        ", Requested: " + request.getPoints());
            }
        }

        int newBalance = switch (request.getTransactionType()) {
            case EARN, BONUS -> currentPoints + request.getPoints();
            case REDEEM, EXPIRE -> currentPoints - request.getPoints();
            case ADJUSTMENT -> request.getPoints();
        };

        customer.setLoyaltyPoints(newBalance);

        LoyaltyTier previousTier = customer.getLoyaltyTier();
        LoyaltyTier newTier = LoyaltyTier.fromPoints(newBalance);
        customer.setLoyaltyTier(newTier);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .customer(customer)
                .transactionType(request.getTransactionType())
                .points(request.getPoints())
                .description(request.getDescription())
                .referenceId(request.getReferenceId())
                .balanceAfter(newBalance)
                .build();

        LoyaltyTransaction saved = transactionRepository.save(transaction);

        if (request.getTransactionType() == TransactionType.EARN) {
            pointsEarnedCounter.increment(request.getPoints());
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            auditLogService.logByEmail(currentUser, AuditAction.POINTS_EARNED,
                    "Customer", customer.getId(),
                    "Points earned: " + request.getPoints() + " for customer " + customer.getEmail());
        }

        if (!previousTier.equals(newTier)) {
            log.info("Tier upgraded for customer {}: {} -> {}",
                    customer.getEmail(), previousTier, newTier);
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            auditLogService.logByEmail(currentUser, AuditAction.TIER_UPGRADED,
                    "Customer", customer.getId(),
                    "Tier upgraded from " + previousTier + " to " + newTier);
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<LoyaltyTransactionResponse> getCustomerTransactions(
            Long customerId, int page, int size) {
        customerService.findById(customerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::mapToResponse);
    }

    private LoyaltyTransactionResponse mapToResponse(LoyaltyTransaction tx) {
        return LoyaltyTransactionResponse.builder()
                .id(tx.getId())
                .customerId(tx.getCustomer().getId())
                .customerName(tx.getCustomer().getName())
                .transactionType(tx.getTransactionType())
                .points(tx.getPoints())
                .description(tx.getDescription())
                .referenceId(tx.getReferenceId())
                .balanceAfter(tx.getBalanceAfter())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
