package com.loyaltyplatform.repository;

import com.loyaltyplatform.entity.Customer;
import com.loyaltyplatform.enums.LoyaltyTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>,
        JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Customer> findByActiveTrue(Pageable pageable);

    Page<Customer> findByCity(String city, Pageable pageable);

    Page<Customer> findByLoyaltyTier(LoyaltyTier tier, Pageable pageable);

    @Query(value = "SELECT c FROM Customer c WHERE " +
                   "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(c.city) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "AND (:city IS NULL OR c.city = :city) " +
                   "AND (:tier IS NULL OR c.loyaltyTier = :tier) " +
                   "AND (:fromDate IS NULL OR c.lastPurchaseDate >= :fromDate) " +
                   "AND (:toDate IS NULL OR c.lastPurchaseDate <= :toDate) " +
                   "AND c.active = true",
           countQuery = "SELECT count(c) FROM Customer c WHERE " +
                        "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(c.city) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:city IS NULL OR c.city = :city) " +
                        "AND (:tier IS NULL OR c.loyaltyTier = :tier) " +
                        "AND (:fromDate IS NULL OR c.lastPurchaseDate >= :fromDate) " +
                        "AND (:toDate IS NULL OR c.lastPurchaseDate <= :toDate) " +
                        "AND c.active = true")
    Page<Customer> searchCustomers(
            @Param("search") String search,
            @Param("city") String city,
            @Param("tier") LoyaltyTier tier,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.loyaltyTier = :tier AND c.active = true")
    long countByLoyaltyTier(@Param("tier") LoyaltyTier tier);

    @Query("SELECT SUM(c.lifetimeValue) FROM Customer c WHERE c.active = true")
    BigDecimal sumLifetimeValue();

    @Query("SELECT c FROM Customer c WHERE c.lifetimeValue >= :minValue AND c.active = true " +
           "ORDER BY c.lifetimeValue DESC")
    List<Customer> findHighValueCustomers(@Param("minValue") BigDecimal minValue, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.lastPurchaseDate < :cutoffDate AND c.active = true")
    List<Customer> findInactiveCustomers(@Param("cutoffDate") LocalDate cutoffDate, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.lastPurchaseDate >= :recentDate AND c.active = true " +
           "ORDER BY c.lastPurchaseDate DESC")
    List<Customer> findRecentBuyers(@Param("recentDate") LocalDate recentDate, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt >= :startDate AND c.active = true")
    long countNewCustomersSince(@Param("startDate") java.time.LocalDateTime startDate);
}
