package com.rrsgroup.company.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Log4j2
public class FrontEndLinkService {
    @Value("${app.frontend.base-url}")
    private String appFrontendBaseUrl;

    public String getCustomerLandingPageLink(Long companyId, UUID customerCode) {
        String customerLandingPageLink = appFrontendBaseUrl +"?company="+companyId+"&customerCode="+customerCode;
        log.debug("customerLandingPageLink: {}", customerLandingPageLink);

        return customerLandingPageLink;
    }
}
