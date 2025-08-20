package com.rrsgroup.waitque.repository;

import com.rrsgroup.waitque.entity.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Long> {
}
