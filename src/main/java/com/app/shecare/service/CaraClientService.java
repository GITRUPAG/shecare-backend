package com.app.shecare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CaraClientService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CARA_URL = "http://192.168.0.101:8001/chat/sync";

    public CaraResponse sendMessage(String token, String message) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(Map.of("message", message), headers);

        try {
            ResponseEntity<String> res =
                    restTemplate.postForEntity(CARA_URL, request, String.class);

            String body = res.getBody();

            if (body == null || body.isBlank()) {
                return new CaraResponse("I'm here for you 💗", "neutral", false);
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

    public record CaraResponse(String reply, String emotion, boolean crisis) {}
}