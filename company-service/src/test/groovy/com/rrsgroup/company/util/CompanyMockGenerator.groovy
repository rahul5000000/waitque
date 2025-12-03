package com.rrsgroup.company.util

import com.rrsgroup.common.dto.EmailDto
import com.rrsgroup.common.entity.Address
import com.rrsgroup.common.entity.PhoneNumber
import com.rrsgroup.company.entity.Company
import com.rrsgroup.company.entity.CompanyEmail
import spock.lang.Specification

class CompanyMockGenerator extends Specification {
    Company getCompanyMock() {
        def companyId = 1231L
        def name = "name"
        def logoUrl = "logoUrl.com"
        def landingPrompt = "What do you want to do?"
        def textColor = "#FFFFFF"
        def backgroundColor = "#000000"
        def primaryButtonColor = "#AAAAAA"
        def secondaryButtonColor = "#BBBBBB"
        def warningButtonColor = "#CCCCCC"
        def dangerButtonColor = "#DDDDDD"

        def addressId = 1232L
        def address1 = "address1"
        def address2 = "address2"
        def city = "city"
        def state = "state"
        def zipcode = "zipcode"
        def country = "country"

        def phoneNumberId = 1233L
        def countryCode = 1
        def phoneNumber = 1234567

        def address = new Address(addressId, address1, address2, city, state, zipcode, country)
        def phoneNumberDomain = new PhoneNumber(phoneNumberId, countryCode, phoneNumber)

        return new Company(companyId, name, logoUrl, landingPrompt, textColor, backgroundColor, primaryButtonColor,
                secondaryButtonColor, warningButtonColor, dangerButtonColor, address, phoneNumberDomain, new ArrayList<CompanyEmail>());
    }
}
