package com.rrsgroup.customer.repository;

import com.rrsgroup.customer.domain.message.MessageStatus;
import com.rrsgroup.customer.entity.message.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m JOIN m.customer cust JOIN cust.crmConfig crmc WHERE crmc.companyId = :companyId")
    Page<Message> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT m FROM Message m JOIN m.customer cust JOIN cust.crmConfig crmc WHERE crmc.companyId = :companyId AND m.status IN (:statuses)")
    Page<Message> findByCompanyIdAndStatusIn(@Param("companyId") Long companyId, @Param("statuses") Collection<MessageStatus> statuses, Pageable pageable);
}
