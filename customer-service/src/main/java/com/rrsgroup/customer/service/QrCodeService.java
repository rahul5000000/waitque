package com.rrsgroup.customer.service;

import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.QrCode;
import com.rrsgroup.customer.repository.QrCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class QrCodeService {
    private final QrCodeRepository qrCodeRepository;

    @Autowired
    public QrCodeService(QrCodeRepository qrCodeRepository) {
        this.qrCodeRepository = qrCodeRepository;
    }

    public List<QrCode> generateUnassignedQrCodes(Integer count, Long companyId, String createdByUserId) {
        LocalDateTime now = LocalDateTime.now();
        List<QrCode> qrCodesToSave = IntStream.range(0, count)
                .parallel()
                .mapToObj(i -> QrCode.builder().companyId(companyId).qrCode(UUID.randomUUID()).createdDate(now).createdBy(createdByUserId).updatedDate(now).updatedBy(createdByUserId).build())
                .toList();
        return qrCodeRepository.saveAllAndFlush(qrCodesToSave);
    }

    public List<QrCode> getQrCodesForCustomers(List<Customer> customers) {
        return qrCodeRepository.findAllByCustomerIn(customers);
    }

    public Optional<QrCode> getQrCode(UUID code, CompanyUserDto userDto) {
        return qrCodeRepository.findByQrCodeAndCompanyId(code, userDto.getCompanyId());
    }
    
    public Optional<QrCode> getQrCodeForCustomer(Customer customer) {
        return qrCodeRepository.findByCustomer(customer);
    }

    public QrCode associateQrCodeToCustomer(QrCode qrCode, Customer customer) {
        Optional<QrCode> currentState = qrCodeRepository.findById(qrCode.getId());

        if(currentState.get().getCustomer() != null) {
            Customer currentCustomer = currentState.get().getCustomer();

            if(!currentCustomer.getId().equals(customer.getId())) {
                throw new IllegalUpdateException("The QR code is already assigned to another customer");
            }
        }

        qrCode.setCustomer(customer);

        return qrCodeRepository.save(qrCode);
    }

    public QrCode disassociateQrCode(QrCode qrCode) {
        Optional<QrCode> currentState = qrCodeRepository.findById(qrCode.getId());

        if(currentState.get().getCustomer() == null) {
            // The QrCode is already disassociated
            return qrCode;
        }

        qrCode.setCustomer(null);
        qrCodeRepository.save(qrCode);

        return qrCode;
    }
}
