package com.bida.service;

import com.bida.entity.Customer;
import com.bida.entity.enums.MembershipTier;
import com.bida.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllByOrderByTotalSpentDesc();
    }

    @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay khach hang: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Customer> getByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }

    @Transactional(readOnly = true)
    public List<Customer> search(String keyword) {
        return customerRepository.search(keyword);
    }

    @Transactional(readOnly = true)
    public List<Customer> getTopVIP() {
        return customerRepository.findTop10ByOrderByTotalSpentDesc();
    }

    public Customer createCustomer(String name, String phone, String email) {
        if (customerRepository.existsByPhone(phone)) {
            throw new RuntimeException("So dien thoai da ton tai: " + phone);
        }
        Customer customer = Customer.builder()
                .name(name)
                .phone(phone)
                .email(email)
                .membershipTier(MembershipTier.BRONZE)
                .totalSpent(BigDecimal.ZERO)
                .points(0)
                .build();
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, String name, String phone, String email) {
        Customer customer = getById(id);
        customer.setName(name);
        customer.setPhone(phone);
        customer.setEmail(email);
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
