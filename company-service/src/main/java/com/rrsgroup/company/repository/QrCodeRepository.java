package com.rrsgroup.company.repository;

import com.rrsgroup.company.entity.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
}
