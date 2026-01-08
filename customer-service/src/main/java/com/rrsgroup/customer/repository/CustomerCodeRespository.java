package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.entity.CustomerCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerCodeRespository extends JpaRepository<CustomerCode, Long> {
    Optional<CustomerCode> findByCustomerCode(String customerCode);
}
