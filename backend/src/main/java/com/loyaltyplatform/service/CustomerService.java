package com.loyaltyplatform.service;

import com.loyaltyplatform.dto.request.CustomerRequest;
import com.loyaltyplatform.dto.response.CustomerResponse;
import com.loyaltyplatform.entity.Customer;
import com.loyaltyplatform.enums.AuditAction;
import com.loyaltyplatform.enums.LoyaltyTier;
import com.loyaltyplatform.exception.BadRequestException;
import com.loyaltyplatform.exception.DuplicateResourceException;
import com.loyaltyplatform.exception.ResourceNotFoundException;
import com.loyaltyplatform.repository.CustomerRepository;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;
    private final Counter customerCreatedCounter;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Customer already exists with email: " + request.getEmail());
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .city(request.getCity())
                .lifetimeValue(request.getLifetimeValue() != null ?
                        request.getLifetimeValue() : BigDecimal.ZERO)
                .lastPurchaseDate(request.getLastPurchaseDate())
                .notes(request.getNotes())
                .loyaltyPoints(0)
                .loyaltyTier(LoyaltyTier.SILVER)
                .active(true)
                .build();

        Customer saved = customerRepository.save(customer);
        customerCreatedCounter.increment();

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.logByEmail(currentUser, AuditAction.CUSTOMER_CREATED,
                "Customer", saved.getId(), "Customer created: " + saved.getEmail());

        log.info("Customer created: id={}, email={}", saved.getId(), saved.getEmail());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        return mapToResponse(findById(id));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = findById(id);

        if (!customer.getEmail().equals(request.getEmail()) &&
                customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already in use: " + request.getEmail());
        }

        String oldValue = customer.getEmail();

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCity(request.getCity());
        if (request.getLifetimeValue() != null) {
            customer.setLifetimeValue(request.getLifetimeValue());
        }
        if (request.getLastPurchaseDate() != null) {
            customer.setLastPurchaseDate(request.getLastPurchaseDate());
        }
        customer.setNotes(request.getNotes());

        Customer updated = customerRepository.save(customer);

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.logByEmail(currentUser, AuditAction.CUSTOMER_UPDATED,
                "Customer", id, "Customer updated", oldValue, request.getEmail());

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = findById(id);
        customer.setActive(false);
        customerRepository.save(customer);

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.logByEmail(currentUser, AuditAction.CUSTOMER_DELETED,
                "Customer", id, "Customer soft-deleted: " + customer.getEmail());
        log.info("Customer soft-deleted: id={}", id);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(
            String search, String city, LoyaltyTier tier,
            LocalDate fromDate, LocalDate toDate,
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Customer> spec = (root, query, cb) -> cb.isTrue(root.get("active"));

        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("city")), like)
            ));
        }
        if (city != null && !city.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("city"), city));
        }
        if (tier != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("loyaltyTier"), tier));
        }
        if (fromDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("lastPurchaseDate"), fromDate));
        }
        if (toDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("lastPurchaseDate"), toDate));
        }

        return customerRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .filter(Customer::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    public CustomerResponse mapToResponse(Customer customer) {
        LoyaltyTier tier = customer.getLoyaltyTier();
        int points = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;

        int nextTierMin = 0;
        int currentTierMin = tier.getMinPoints();

        if (tier == LoyaltyTier.SILVER) {
            nextTierMin = LoyaltyTier.GOLD.getMinPoints();
        } else if (tier == LoyaltyTier.GOLD) {
            nextTierMin = LoyaltyTier.PLATINUM.getMinPoints();
        } else {
            nextTierMin = points;
            currentTierMin = LoyaltyTier.PLATINUM.getMinPoints();
        }

        double progress = tier == LoyaltyTier.PLATINUM ? 100.0 :
                Math.min(100.0, ((double)(points - currentTierMin) /
                        (nextTierMin - currentTierMin)) * 100);

        int pointsToNext = tier == LoyaltyTier.PLATINUM ? 0 :
                Math.max(0, nextTierMin - points);

        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .city(customer.getCity())
                .lifetimeValue(customer.getLifetimeValue())
                .loyaltyPoints(points)
                .loyaltyTier(tier)
                .loyaltyTierDisplayName(tier.getDisplayName())
                .lastPurchaseDate(customer.getLastPurchaseDate())
                .notes(customer.getNotes())
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .nextTierProgress(Math.round(progress * 10.0) / 10.0)
                .pointsToNextTier(pointsToNext)
                .build();
    }
}
