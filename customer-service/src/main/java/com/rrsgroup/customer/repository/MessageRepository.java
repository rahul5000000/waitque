package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.entity.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
