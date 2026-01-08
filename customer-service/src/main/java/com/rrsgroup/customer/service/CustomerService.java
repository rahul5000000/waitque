package com.rrsgroup.customer.service;

import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.FileStage;
import com.rrsgroup.customer.domain.UploadFileType;
import com.rrsgroup.customer.entity.CrmConfig;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.CustomerCode;
import com.rrsgroup.customer.entity.QrCode;
import com.rrsgroup.customer.repository.CustomerCodeRespository;
import com.rrsgroup.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CustomerService {
    private static final String CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private final CustomerRepository customerRepository;
    private final QrCodeService qrCodeService;
    private final CustomerCodeRespository customerCodeRespository;

    @Autowired
    public CustomerService(
            CustomerRepository customerRepository,
            QrCodeService qrCodeService,
            CustomerCodeRespository customerCodeRespository) {
        this.customerRepository = customerRepository;
        this.qrCodeService = qrCodeService;
        this.customerCodeRespository = customerCodeRespository;
    }

    public Customer createCustomer(CrmConfig crmConfig, CrmCustomer crmCustomer, CompanyUserDto createdBy) {
        LocalDateTime now = LocalDateTime.now();
        String userId = createdBy.getUserId();

        Customer customer = Customer.builder()
                .crmCustomerId(crmCustomer.getCrmCustomerId())
                .crmConfig(crmConfig)
                .createdBy(userId)
                .createdDate(now)
                .updatedBy(userId)
                .updatedDate(now)
                .build();

        CustomerCode customerCode = CustomerCode.builder()
                .customer(customer)
                .customerCode(getNewUniqueCustomerCode())
                .status(CustomerCode.CustomerCodeStatus.ACTIVE)
                .createdBy(userId)
                .createdDate(now)
                .updatedBy(userId)
                .updatedDate(now)
                .build();

        customer.setCustomerCodes(List.of(customerCode));

        return customerRepository.save(customer);
    }

    private String getNewUniqueCustomerCode() {
        // Only try up to 3 times
        for(int i = 0; i < 3; i++) {
            String randomCustomerCode = generateCustomerCode();

            Optional<CustomerCode> existingCustomerCode  = customerCodeRespository.findByCustomerCode(randomCustomerCode);

            if(existingCustomerCode.isEmpty()) {
                return randomCustomerCode;
            }
        }

        throw new RuntimeException("Failed to generate a unique customer code after 3 attempts");
    }

    private String generateCustomerCode() {
        StringBuilder sb = new StringBuilder(8);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < 8; i++) {
            sb.append(CHARACTERS.charAt(
                    random.nextInt(CHARACTERS.length())));
        }

        return sb.toString();
    }

    public Customer getCustomerByCrmConfig(CrmConfig crmConfig, CrmCustomer crmCustomer) {
        return customerRepository.findByCrmCustomerIdAndCrmConfig_Id(crmCustomer.getCrmCustomerId(), crmConfig.getId());
    }

    public List<Customer> getCustomersByCrmConfig(CrmConfig crmConfig, List<CrmCustomer> crmCustomers) {
        List<String> crmCustomerIds = crmCustomers.stream().map(CrmCustomer::getCrmCustomerId).toList();
        return customerRepository.findAllByCrmCustomerIdInAndCrmConfig_Id(crmCustomerIds, crmConfig.getId());
    }

    public Optional<Customer> getCustomerById(Long customerId, CompanyUserDto userDto) {
        return customerRepository.findByIdAndCrmConfig_CompanyId(customerId, userDto.getCompanyId());
    }

    public Optional<Customer> getCustomerById(Long customerId, Long companyId) {
        return customerRepository.findByIdAndCrmConfig_CompanyId(customerId, companyId);
    }

    public Optional<Customer> getCustomerByQrCode(UUID qrCode) {
        Optional<QrCode> qrCodeOptional = qrCodeService.getAssociatedQrCode(qrCode);

        if(qrCodeOptional.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(qrCodeOptional.get().getCustomer());
    }

    public Customer getCustomerByQrCodeSafe(UUID qrCode) {
        Optional<Customer> customerOptional = getCustomerByQrCode(qrCode);

        if(customerOptional.isEmpty()) {
            throw new RecordNotFoundException("Customer does not exist with qrCode=" + qrCode);
        }

        return customerOptional.get();
    }

    public String getBucketKeyForFileAndStage(UUID qrCode, UploadFileType fileType, String fileName, FileStage stage) {
        List<UploadFileType> supportedFileTypes = List.of(UploadFileType.LEAD);

        if(supportedFileTypes.contains(fileType)) {
            return stage.toString() + "/" + fileType.getFolder()+ "/" + qrCode + "/" + fileName;
        } else {
            throw new IllegalRequestException("UploadFileType " + fileType + " is not supported");
        }
    }

    public String getBucketKeyForFileAndStage(Customer customer, UploadFileType fileType, String fileName, FileStage stage) {
        List<UploadFileType> supportedFileTypes = List.of(UploadFileType.RESPONSE);

        if(supportedFileTypes.contains(fileType)) {
            return stage.toString() + "/" + fileType.getFolder()+ "/" + customer.getId() + "/" + fileName;
        } else {
            throw new IllegalRequestException("UploadFileType " + fileType + " is not supported");
        }
    }

    public QrCode getQrCodeForCustomerCode(String customerCode) {
        Optional<CustomerCode> customerCodeOptional = customerCodeRespository.findByCustomerCode(customerCode);

        if(customerCodeOptional.isEmpty()) {
            throw new RecordNotFoundException("Customer code is invalid");
        }

        Customer customer = customerCodeOptional.get().getCustomer();

        Optional<QrCode> qrCodeOptional = qrCodeService.getQrCodeForCustomer(customer);

        if(qrCodeOptional.isEmpty()) {
            throw new IllegalRequestException("Customer is not associated with a QR code");
        }

        return qrCodeOptional.get();
    }
}
