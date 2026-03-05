package com.app.shecare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.app.shecare.dto.PcosPredictionRequest;
import com.app.shecare.dto.PcosPredictionResponse;

import java.util.HashMap;
import java.util.Map;
import com.app.shecare.dto.ToxicityResponse;
import com.app.shecare.entity.PeriodLog;
import com.app.shecare.entity.User;


@Service
@RequiredArgsConstructor
public class AiPredictionService {

    @Value("${ai.service.url}")
    private String aiUrl;

    private final WebClient webClient = WebClient.create();

    public Object predictFromDates(Object request) {

        return webClient.post()
                .uri(aiUrl + "/predict/from-dates")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    public Object getPhaseInsights(Object request) {

        return webClient.post()
                .uri(aiUrl + "/insights/phase")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Object.class)
                .block();
    }

    public PcosPredictionResponse predictPcos(PcosPredictionRequest request) {

    Map<String, Object> body = new HashMap<>();

body.put("age", (int) request.getAge());
body.put("bmi", request.getBmi());
body.put("cycle_length", (int) request.getCycle_length());
body.put("cycle_regularity", request.getCycle_regularity());

body.put("follicle_no_right", request.getFollicle_no_right());
body.put("follicle_no_left", request.getFollicle_no_left());

body.put("amh", request.getAmh());
body.put("fsh", request.getFsh());
body.put("lh", request.getLh());
body.put("fsh_lh_ratio", request.getFsh_lh_ratio());

body.put("waist_hip_ratio", request.getWaist_hip_ratio());
body.put("endometrium_mm", request.getEndometrium_mm());

body.put("avg_follicle_size_r", request.getAvg_follicle_size_r());
body.put("avg_follicle_size_l", request.getAvg_follicle_size_l());

body.put("weight_gain", request.getWeight_gain());
body.put("hair_growth", request.getHair_growth());
body.put("skin_darkening", request.getSkin_darkening());
body.put("pimples", request.getPimples());
    System.out.println("PCOS REQUEST BODY: " + body);

    return webClient.post()
            .uri(aiUrl + "/predict/pcos")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(PcosPredictionResponse.class)
            .block();
}

    public boolean isToxic(String text) {

        var response = webClient.post()
                .uri(aiUrl + "/moderate/text")
                .bodyValue(Map.of("text", text))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return (Boolean) response.get("toxic");
    }

    public ToxicityResponse checkToxicity(String text) {

        return webClient.post()
                .uri(aiUrl + "/safety/check-toxicity")
                .bodyValue(Map.of("text", text, "context", "comment"))
                .retrieve()
                .bodyToMono(ToxicityResponse.class)
                .block();
    }

    
}