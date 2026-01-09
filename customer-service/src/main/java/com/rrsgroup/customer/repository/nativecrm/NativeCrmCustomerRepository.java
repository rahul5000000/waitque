package com.rrsgroup.customer.repository.nativecrm;

import com.rrsgroup.customer.entity.nativecrm.NativeCrmCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NativeCrmCustomerRepository extends JpaRepository<NativeCrmCustomer, Long> {
    Optional<NativeCrmCustomer> findByIdAndTenantId(Long id, Long tenantId);
    @Query("""
            SELECT DISTINCT c
            FROM NativeCrmCustomer c
            LEFT JOIN c.address a
            LEFT JOIN c.phoneNumber p
            LEFT JOIN c.email e
            WHERE c.tenantId = :tenantId
                AND (
                    LOWER(c.companyName) LIKE LOWER(CONCAT('%', :companyNameSnippet, '%'))
                    OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstNameSnippet, '%'))
                    OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastNameSnippet, '%'))
                    OR LOWER(a.address1) LIKE LOWER(CONCAT('%', :addressSnippet, '%'))
                    OR LOWER(a.address2) LIKE LOWER(CONCAT('%', :addressSnippet, '%'))
                    OR LOWER(a.city) LIKE LOWER(CONCAT('%', :addressSnippet, '%'))
                    OR LOWER(a.state) LIKE LOWER(CONCAT('%', :addressSnippet, '%'))
                    OR LOWER(a.zipcode) LIKE LOWER(CONCAT('%', :addressSnippet, '%'))
                    OR LOWER(a.country) LIKE LOWER(CONCAT('%', :addressSnippet, '%'))
                    OR CAST(p.countryCode AS string) LIKE LOWER(CONCAT('%', :phoneNumberSnippet, '%'))
                    OR CAST(p.phoneNumber AS string) LIKE LOWER(CONCAT('%', :phoneNumberSnippet, '%'))
                )
            """)
    List<NativeCrmCustomer> searchCustomers(
            @Param("tenantId") Long tenantId,
            @Param("companyNameSnippet") String companyNameSnippet,
            @Param("firstNameSnippet") String firstNameSnippet,
            @Param("lastNameSnippet") String lastNameSnippet,
            @Param("addressSnippet") String addressSnippet,
            @Param("phoneNumberSnippet") String phoneNumberSnippet);
}
