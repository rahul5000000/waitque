package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.entity.lead.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {
}
