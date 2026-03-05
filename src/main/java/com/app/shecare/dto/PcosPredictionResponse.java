package com.app.shecare.dto;

import lombok.Data;
import java.util.List;

@Data
public class PcosPredictionResponse {

    private String prediction;

    private double pcos_probability;

    private String risk_level;

    private String interpretation;

    private List<String> top_risk_factors;

    private String recommendation;

    private String disclaimer;
}