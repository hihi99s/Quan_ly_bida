package com.bida.repository;

import com.bida.entity.Customer;
import com.bida.entity.enums.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhone(String phone);

    boolean existsByPhone(String phone);

    List<Customer> findByMembershipTier(MembershipTier tier);

    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% OR c.phone LIKE %:keyword%")
    List<Customer> search(String keyword);

    List<Customer> findAllByOrderByTotalSpentDesc();

    List<Customer> findTop10ByOrderByTotalSpentDesc();
}
