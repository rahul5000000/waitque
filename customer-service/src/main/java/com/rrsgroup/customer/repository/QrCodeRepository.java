package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    List<QrCode> findAllByCustomerIn(List<Customer> customers);
}
