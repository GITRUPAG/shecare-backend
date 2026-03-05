package com.app.shecare.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HealthRiskResponse {

    private int healthScore;

    private String riskLevel;

    private List<String> factors;

    private List<String> recommendations;
}