package com.rrsgroup.customer.service.message;

import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.common.service.CommonDtoMapper;
import com.rrsgroup.customer.dto.message.MessageDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageDtoMapper {
    private final CommonDtoMapper commonDtoMapper;

    @Autowired
    public MessageDtoMapper(CommonDtoMapper commonDtoMapper) {
        this.commonDtoMapper = commonDtoMapper;
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

        return new MessageDto(message.getId(), message.getCustomer().getCrmConfig().getCompanyId(), message.getStatus(),
                message.getOverrideFirstName(), message.getOverrideLastName(), addressDto, phoneNumberDto,
                message.getOverrideEmail(), message.getMessage(), null, message.getCreatedDate(),
                message.getUpdatedDate(), message.getCreatedBy(), message.getUpdatedBy());
    }
}
