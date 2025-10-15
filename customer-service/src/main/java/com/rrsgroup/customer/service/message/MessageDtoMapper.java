package com.rrsgroup.customer.service.message;

import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.common.service.CommonDtoMapper;
import com.rrsgroup.common.util.PageableWrapper;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.dto.LeadFlowDto;
import com.rrsgroup.customer.dto.lead.LeadListDto;
import com.rrsgroup.customer.dto.message.MessageDto;
import com.rrsgroup.customer.dto.message.MessageListDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.entity.message.Message;
import com.rrsgroup.customer.service.CustomerCrmIntegrationService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j2
public class MessageDtoMapper {
    private final CommonDtoMapper commonDtoMapper;
    private final CustomerCrmIntegrationService customerCrmIntegrationService;

    @Autowired
    public MessageDtoMapper(CommonDtoMapper commonDtoMapper, CustomerCrmIntegrationService customerCrmIntegrationService) {
        this.commonDtoMapper = commonDtoMapper;
        this.customerCrmIntegrationService = customerCrmIntegrationService;
    }

    public Message map(MessageDto dto, Customer customer) {
        Message.MessageBuilder builder = Message.builder().status(dto.status()).message(dto.message())
                .overrideFirstName(dto.overrideFirstName()).overrideLastName(dto.overrideLastName())
                .overrideEmail(dto.overrideEmail()).customer(customer);

        if(dto.overrideAddress() != null) {
            builder.overrideAddress(commonDtoMapper.map(dto.overrideAddress()));
        }

        if(dto.overridePhoneNumber() != null) {
            builder.overridePhoneNumber(commonDtoMapper.map(dto.overridePhoneNumber()));
        }

        return builder.build();
    }

    public MessageDto map(Message message) {
        AddressDto addressDto = message.getOverrideAddress() == null ? null : commonDtoMapper.map(message.getOverrideAddress());
        PhoneNumberDto phoneNumberDto = message.getOverridePhoneNumber() == null ? null : commonDtoMapper.map(message.getOverridePhoneNumber());

        return new MessageDto(message.getId(), message.getStatus(),
                message.getOverrideFirstName(), message.getOverrideLastName(), addressDto, phoneNumberDto,
                message.getOverrideEmail(), message.getMessage(), null, message.getCreatedDate(),
                message.getUpdatedDate(), message.getCreatedBy(), message.getUpdatedBy());
    }

    public MessageListDto map(Page<Message> pageOfMessages) {
        PageableWrapper pageable = new PageableWrapper(pageOfMessages.getPageable());
        return new MessageListDto(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageOfMessages.getTotalElements(),
                pageable.getSortField(),
                pageable.getSortDir(),
                pageOfMessages.getContent().stream().map(message -> {
                    Optional<CrmCustomer> crmCustomerOptional = customerCrmIntegrationService.getCrmCustomer(message.getCustomer().getCrmCustomerId(), message.getCustomer().getCrmConfig());

                    if(crmCustomerOptional.isEmpty()) {
                        log.error("Did not find matching crmCustomer for customerId={}, crmCustomerId={}", message.getCustomer().getId(), message.getCustomer().getCrmCustomerId());
                        return null;
                    }

                    CrmCustomer crmCustomer = crmCustomerOptional.get();

                    String firstName = StringUtils.isBlank(message.getOverrideFirstName()) ? crmCustomer.getFirstName() : message.getOverrideFirstName();
                    String lastName = StringUtils.isBlank(message.getOverrideLastName()) ? crmCustomer.getLastName() : message.getOverrideLastName();
                    String phoneNumber = message.getOverridePhoneNumber() == null ? crmCustomer.getPhoneNumber().toString() : message.getOverridePhoneNumber().toString();
                    String email = StringUtils.isBlank(message.getOverrideEmail()) ? crmCustomer.getEmail() : message.getOverrideEmail();

                    return new MessageListDto.MessageListItem(message.getId(), firstName, lastName, phoneNumber, email, message.getStatus(), message.getCreatedDate(), message.getUpdatedDate());
                }).toList()
        );
    }
}
