package com.rrsgroup.customer.web;

import com.rrsgroup.customer.dto.QrCodeDto;
import com.rrsgroup.customer.service.QrCodeDtoMapper;
import com.rrsgroup.customer.service.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class QrCodeController {
    private final QrCodeService qrCodeService;
    private final QrCodeDtoMapper qrCodeDtoMapper;

    @Autowired
    public QrCodeController(QrCodeService qrCodeService, QrCodeDtoMapper qrCodeDtoMapper) {
        this.qrCodeService = qrCodeService;
        this.qrCodeDtoMapper = qrCodeDtoMapper;
    }

    @GetMapping("/api/system/qrcodes")
    public List<QrCodeDto> generateAssignableQrCodes(
            @RequestParam(name = "count") Integer count,
            @RequestParam(name = "companyId") Long companyId,
            @RequestParam(name = "userId") String userId
    ) {
        return qrCodeDtoMapper.map(qrCodeService.generateUnassignedQrCodes(count, companyId, userId));
    }
}
