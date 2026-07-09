package com.loyaltyplatform.dto.response;

import com.loyaltyplatform.enums.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransactionResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private TransactionType transactionType;
    private Integer points;
    private String description;
    private String referenceId;
    private Integer balanceAfter;
    private LocalDateTime createdAt;
}
