package com.rrsgroup.waitque.repository;

import com.rrsgroup.waitque.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
