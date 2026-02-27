package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.entity.event.GenericEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GenericEventRepository extends JpaRepository<GenericEvent, UUID> {
}
