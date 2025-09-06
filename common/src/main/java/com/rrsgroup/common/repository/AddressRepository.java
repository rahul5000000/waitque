package com.rrsgroup.common.repository;

import com.rrsgroup.common.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
