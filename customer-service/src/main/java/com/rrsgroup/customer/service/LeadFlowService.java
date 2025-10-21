package com.rrsgroup.customer.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.customer.dto.ActiveLeadFlowListDto;
import com.rrsgroup.customer.dto.LeadFlowDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Service
public class LeadFlowService {
    private final WebClient webClient;

    @Value("${microservices.company-service.base-url}")
    private String companyServiceBaseUrl;

    @Autowired
    public LeadFlowService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Optional<LeadFlowDto> getLeadFlow(Long leadFlowId, Long companyId) {
        try {
            return Optional.ofNullable(webClient.get()
                    .uri(companyServiceBaseUrl + "/api/system/companies/" + companyId + "/flows/" + leadFlowId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<LeadFlowDto>() {
                    })
                    .block());
        } catch (WebClientResponseException.NotFound e) {
            return Optional.empty();
        }
    }

    public Optional<ActiveLeadFlowListDto> getLeadFlows(Long companyId, Integer limit, Integer page,
                                                        String sortField, SortDirection sortDir) {
        try {
            return Optional.ofNullable(webClient.get()
                    .uri(companyServiceBaseUrl + "/api/system/companies/" + companyId + "/flows?limit="+limit+"&page="+page+"&sortField="+sortField+"&sortDir="+sortDir)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ActiveLeadFlowListDto>() {
                    })
                    .block());
        } catch (WebClientResponseException.NotFound e) {
            return Optional.empty();
        }
    }
}
