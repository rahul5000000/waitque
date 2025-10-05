package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByCrmCustomerIdAndCrmConfig_Id(String crmCustomerId, Long crmConfigId);
    List<Customer> findAllByCrmCustomerIdInAndCrmConfig_Id(List<String> crmCustomerIds, Long crmConfigId);
}
