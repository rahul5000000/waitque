package com.rrsgroup.customer.web;

import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.customer.domain.QrCodeSearchRequest;
import com.rrsgroup.customer.dto.QrCodeDto;
import com.rrsgroup.customer.entity.QrCode;
import com.rrsgroup.customer.service.CustomerService;
import com.rrsgroup.customer.service.QrCodeDtoMapper;
import com.rrsgroup.customer.service.QrCodeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class QrCodeController {
    private final QrCodeService qrCodeService;
    private final QrCodeDtoMapper qrCodeDtoMapper;
    private final CustomerService customerService;

    @Autowired
    public QrCodeController(QrCodeService qrCodeService, QrCodeDtoMapper qrCodeDtoMapper, CustomerService customerService) {
        this.qrCodeService = qrCodeService;
        this.qrCodeDtoMapper = qrCodeDtoMapper;
        this.customerService = customerService;
    }

    @GetMapping("/api/system/qrcodes")
    public List<QrCodeDto> generateAssignableQrCodes(
            @RequestParam(name = "count") Integer count,
            @RequestParam(name = "companyId") Long companyId,
            @RequestParam(name = "userId") String userId
    ) {
        if(count == null || count <= 0) {
            throw new IllegalRequestException("Count must be a positive integer");
        }

        if(count > 1000) {
            throw new IllegalRequestException("Count must not exceed 1000");
        }

        return qrCodeDtoMapper.map(qrCodeService.generateUnassignedQrCodes(count, companyId, userId));
    }

    @PostMapping(value = "/api/public/qrCodes/search", consumes = "application/x-www-form-urlencoded")
    public List<QrCodeDto> QrCodeSearch(@ModelAttribute QrCodeSearchRequest request) {
        if(StringUtils.isBlank(request.getCustomerCode())) {
            throw new IllegalRequestException("At least one search parameter must be passed");
        }

        QrCode qrCode = customerService.getQrCodeForCustomerCode(request.getCustomerCode());

        return qrCodeDtoMapper.map(List.of(qrCode));
    }
}
