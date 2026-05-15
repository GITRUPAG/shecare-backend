package com.app.shecare.service;

import com.app.shecare.dto.ai.PeriodDatesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${ai.service.url}")
    private String aiUrl;

    private final WebClient webClient = WebClient.create();

    public Object predictFromDates(PeriodDatesRequest request) {

        return webClient.post()
                .uri(aiUrl + "/predict/from-dates")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }
}