package com.rrsgroup.waitque.repository;

import com.rrsgroup.waitque.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
