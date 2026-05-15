package com.app.shecare.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyInsightResponse {

    private String phase;

    private String energyLevel;

    private String recommendation;

    private String tip;

}