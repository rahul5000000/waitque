package com.rrsgroup.customer.service;

import com.rrsgroup.customer.dto.QrCodeDto;
import com.rrsgroup.customer.entity.QrCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QrCodeDtoMapper {
    public List<QrCodeDto> map(List<QrCode> qrCodes) {
        return qrCodes.stream().map(qrCode -> {
            Long customerId = null;

            if(qrCode.getCustomer() != null) {
                customerId = qrCode.getCustomer().getId();
            }

            return new QrCodeDto(qrCode.getId(), qrCode.getCompanyId(), customerId, qrCode.getQrCode(), qrCode.getCreatedDate(), qrCode.getUpdatedDate(), qrCode.getCreatedBy(), qrCode.getUpdatedBy());
        }).toList();
    }
}
