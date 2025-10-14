package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    List<QrCode> findAllByCustomerIn(List<Customer> customers);
    Optional<QrCode> findByCustomer(Customer customer);
    Optional<QrCode> findByQrCodeAndCompanyId(UUID qrCode, Long companyId);
    Optional<QrCode> findByQrCodeAndCustomerIsNotNull(UUID qrCode);
}
