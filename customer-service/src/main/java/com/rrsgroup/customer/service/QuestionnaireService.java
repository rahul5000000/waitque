package com.rrsgroup.customer.service;

import com.rrsgroup.customer.dto.questionnaire.QuestionnaireDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Service
public class QuestionnaireService {
    private final WebClient webClient;

    @Value("${microservices.company-service.base-url}")
    private String companyServiceBaseUrl;

    @Autowired
    public QuestionnaireService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Optional<QuestionnaireDto> getQuestionnaire(Long questionnaireId, Long companyId) {
        try {
            return Optional.ofNullable(webClient.get()
                    .uri(companyServiceBaseUrl + "/api/system/companies/" + companyId + "/questionnaires/" + questionnaireId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<QuestionnaireDto>() {
                    })
                    .block());
        } catch (WebClientResponseException.NotFound e) {
            return Optional.empty();
        }
    }
}
