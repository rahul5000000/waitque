package com.rrsgroup.company.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FrontEndLinkService {
    public String getCustomerLandingPageLink(Long companyId, UUID customerCode) {
        return "https://waitque.com/web/landing?company="+companyId+"&customerCode="+customerCode;
    }
}
