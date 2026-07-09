package com.loyaltyplatform.service;

import com.loyaltyplatform.dto.request.CustomerRequest;
import com.loyaltyplatform.dto.response.CustomerResponse;
import com.loyaltyplatform.entity.Customer;
import com.loyaltyplatform.enums.LoyaltyTier;
import com.loyaltyplatform.exception.DuplicateResourceException;
import com.loyaltyplatform.exception.ResourceNotFoundException;
import com.loyaltyplatform.repository.CustomerRepository;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private Counter customerCreatedCounter;

    @InjectMocks private CustomerService customerService;

    private Customer testCustomer;
    private CustomerRequest customerRequest;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("Jane Smith")
                .email("jane@example.com")
                .phone("+1234567890")
                .city("New York")
                .lifetimeValue(BigDecimal.valueOf(1500.00))
                .loyaltyPoints(500)
                .loyaltyTier(LoyaltyTier.SILVER)
                .active(true)
                .build();

        customerRequest = CustomerRequest.builder()
                .name("Jane Smith")
                .email("jane@example.com")
                .phone("+1234567890")
                .city("New York")
                .lifetimeValue(BigDecimal.valueOf(1500.00))
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
    @DisplayName("Should create customer successfully")
    void shouldCreateCustomer() {
        when(customerRepository.existsByEmail(customerRequest.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        doNothing().when(customerCreatedCounter).increment();
        doNothing().when(auditLogService).logByEmail(any(), any(), any(), any(), any());

        CustomerResponse response = customerService.createCustomer(customerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Jane Smith");
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getLoyaltyTier()).isEqualTo(LoyaltyTier.SILVER);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowOnDuplicateEmail() {
        when(customerRepository.existsByEmail(customerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(customerRequest))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Should return customer by ID")
    void shouldGetCustomerById() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        CustomerResponse response = customerService.getCustomerById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown ID")
    void shouldThrowForUnknownId() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should soft-delete customer")
    void shouldSoftDeleteCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any())).thenReturn(testCustomer);
        doNothing().when(auditLogService).logByEmail(any(), any(), any(), any(), any());

        customerService.deleteCustomer(1L);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    @DisplayName("Should calculate tier progress correctly")
    void shouldCalculateTierProgress() {
        testCustomer.setLoyaltyPoints(500);
        testCustomer.setLoyaltyTier(LoyaltyTier.SILVER);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        CustomerResponse response = customerService.getCustomerById(1L);

        assertThat(response.getNextTierProgress()).isGreaterThan(0);
        assertThat(response.getPointsToNextTier()).isGreaterThan(0);
    }
}
