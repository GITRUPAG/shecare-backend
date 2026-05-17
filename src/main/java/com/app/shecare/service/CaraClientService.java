package com.app.shecare.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaraClientService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cara.service.url}")
    private String caraUrl;

    public CaraResponse sendMessage(String token, String message) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(Map.of("message", message), headers);

        try {

            ResponseEntity<String> res =
                    restTemplate.postForEntity(
                            
                            caraUrl + "/chat/sync",
                            request,
                            String.class
                    );

           System.out.println("Calling Cara URL: " + caraUrl + "/chat/sync");
System.out.println("Token: " + token);
System.out.println("Message: " + message);
            String body = res.getBody();

            if (body == null || body.isBlank()) {
                return new CaraResponse(
                        "I'm here for you 💗",
                        "neutral",
                        false
                );
            }

            JsonNode json = objectMapper.readTree(body);

            return new CaraResponse(
                    json.path("reply").asText("I'm here for you 💗"),
                    json.path("emotion").asText("neutral"),
                    json.path("crisis").asBoolean(false)
            );

        } catch (Exception e) {

            return new CaraResponse(
                    "I'm having a small delay, but I'm here 💗",
                    "neutral",
                    false
            );
        }
    }

    public record CaraResponse(
            String reply,
            String emotion,
            boolean crisis
    ) {}
}