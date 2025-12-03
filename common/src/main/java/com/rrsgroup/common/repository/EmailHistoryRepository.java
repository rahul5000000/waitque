package com.rrsgroup.common.repository;

import com.rrsgroup.common.entity.EmailHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailHistoryRepository extends JpaRepository<EmailHistory, Long> {
}
