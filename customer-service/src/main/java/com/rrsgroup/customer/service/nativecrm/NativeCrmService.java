package com.rrsgroup.customer.service.nativecrm;

import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.CustomerSearchRequest;
import com.rrsgroup.customer.service.CrmService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("waitqueCrmService")
public class NativeCrmService implements CrmService {
    @Override
    public Optional<CrmCustomer> getCustomerById(String crmCustomerId) {
        return Optional.empty();
    }

    @Override
    public List<CrmCustomer> searchCustomers(CustomerSearchRequest request) {
        return List.of();
    }
}
