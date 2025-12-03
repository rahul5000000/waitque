package com.rrsgroup.company.dto;

import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.EmailDto;
import com.rrsgroup.common.dto.PhoneNumberDto;

public record CompanyDto(Long id, String name, AddressDto address, PhoneNumberDto phoneNumber, String logoUrl,
                         String landingPrompt, String textColor, String backgroundColor, String primaryButtonColor,
                         String secondaryButtonColor, String warningButtonColor, String dangerButtonColor,
                         EmailDto messageNotificationEmail, EmailDto leadNotificationEmail) {
}
