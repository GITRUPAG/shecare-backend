package com.app.shecare.dto.ai;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PhaseInsightRequest {

    private String phase;

    private Integer age;

    private Map<String, Double> symptoms;

    private List<String> healthGoals;

    private List<String> conditions;

}