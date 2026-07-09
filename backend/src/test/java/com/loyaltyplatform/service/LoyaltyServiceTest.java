package com.loyaltyplatform.service;

import com.loyaltyplatform.dto.request.LoyaltyTransactionRequest;
import com.loyaltyplatform.dto.response.LoyaltyTransactionResponse;
import com.loyaltyplatform.entity.Customer;
import com.loyaltyplatform.entity.LoyaltyTransaction;
import com.loyaltyplatform.enums.LoyaltyTier;
import com.loyaltyplatform.enums.TransactionType;
import com.loyaltyplatform.exception.BadRequestException;
import com.loyaltyplatform.repository.LoyaltyTransactionRepository;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoyaltyService Unit Tests")
class LoyaltyServiceTest {

    @Mock private LoyaltyTransactionRepository transactionRepository;
    @Mock private CustomerService customerService;
    @Mock private AuditLogService auditLogService;
    @Mock private Counter pointsEarnedCounter;

    @InjectMocks private LoyaltyService loyaltyService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .loyaltyPoints(200)
                .loyaltyTier(LoyaltyTier.SILVER)
                .active(true)
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@example.com");
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should earn points and update balance")
    void shouldEarnPoints() {
        LoyaltyTransactionRequest request = LoyaltyTransactionRequest.builder()
                .customerId(1L)
                .transactionType(TransactionType.EARN)
                .points(300)
                .description("Purchase reward")
                .build();

        LoyaltyTransaction savedTx = LoyaltyTransaction.builder()
                .id(1L)
                .customer(testCustomer)
                .transactionType(TransactionType.EARN)
                .points(300)
                .balanceAfter(500)
                .build();

        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(transactionRepository.save(any())).thenReturn(savedTx);
        doNothing().when(pointsEarnedCounter).increment(anyDouble());
        doNothing().when(auditLogService).logByEmail(any(), any(), any(), any(), any());

        LoyaltyTransactionResponse response = loyaltyService.processTransaction(request);

        assertThat(response.getBalanceAfter()).isEqualTo(500);
        assertThat(testCustomer.getLoyaltyPoints()).isEqualTo(500);
    }

    @Test
    @DisplayName("Should redeem points when balance is sufficient")
    void shouldRedeemPoints() {
        testCustomer.setLoyaltyPoints(500);

        LoyaltyTransactionRequest request = LoyaltyTransactionRequest.builder()
                .customerId(1L)
                .transactionType(TransactionType.REDEEM)
                .points(200)
                .description("Redemption")
                .build();

        LoyaltyTransaction savedTx = LoyaltyTransaction.builder()
                .id(1L)
                .customer(testCustomer)
                .transactionType(TransactionType.REDEEM)
                .points(200)
                .balanceAfter(300)
                .build();

        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(transactionRepository.save(any())).thenReturn(savedTx);
        doNothing().when(auditLogService).logByEmail(any(), any(), any(), any(), any());

        LoyaltyTransactionResponse response = loyaltyService.processTransaction(request);

        assertThat(response.getBalanceAfter()).isEqualTo(300);
    }

    @Test
    @DisplayName("Should throw BadRequestException when insufficient points for redemption")
    void shouldThrowOnInsufficientPoints() {
        testCustomer.setLoyaltyPoints(100);

        LoyaltyTransactionRequest request = LoyaltyTransactionRequest.builder()
                .customerId(1L)
                .transactionType(TransactionType.REDEEM)
                .points(500)
                .build();

        when(customerService.findById(1L)).thenReturn(testCustomer);

        assertThatThrownBy(() -> loyaltyService.processTransaction(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient points");
    }

    @Test
    @DisplayName("Should upgrade tier when points cross threshold")
    void shouldUpgradeTierOnThreshold() {
        testCustomer.setLoyaltyPoints(950);
        testCustomer.setLoyaltyTier(LoyaltyTier.SILVER);

        LoyaltyTransactionRequest request = LoyaltyTransactionRequest.builder()
                .customerId(1L)
                .transactionType(TransactionType.EARN)
                .points(100)
                .build();

        LoyaltyTransaction savedTx = LoyaltyTransaction.builder()
                .id(1L)
                .customer(testCustomer)
                .transactionType(TransactionType.EARN)
                .points(100)
                .balanceAfter(1050)
                .build();

        when(customerService.findById(1L)).thenReturn(testCustomer);
        when(transactionRepository.save(any())).thenReturn(savedTx);
        doNothing().when(pointsEarnedCounter).increment(anyDouble());
        doNothing().when(auditLogService).logByEmail(any(), any(), any(), any(), any());

        loyaltyService.processTransaction(request);

        assertThat(testCustomer.getLoyaltyTier()).isEqualTo(LoyaltyTier.GOLD);
    }
}
