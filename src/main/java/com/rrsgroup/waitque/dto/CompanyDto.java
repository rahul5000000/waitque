package com.rrsgroup.waitque.dto;

public record CompanyDto(Long id, String name, AddressDto address, PhoneNumberDto phoneNumber, String logoUrl,
                         String landingPrompt, String textColor, String backgroundColor, String primaryButtonColor,
                         String secondaryButtonColor, String warningButtonColor, String dangerButtonColor) {
}
