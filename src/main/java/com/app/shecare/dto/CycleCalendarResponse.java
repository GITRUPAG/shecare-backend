package com.app.shecare.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CycleCalendarResponse {

    private String currentPhase;

    private String nextPeriodStart;

    private String nextPeriodEnd;

    private String ovulationDay;

    private String fertileStart;

    private String fertileEnd;

    private String pmsStart;

    private String pmsEnd;
}