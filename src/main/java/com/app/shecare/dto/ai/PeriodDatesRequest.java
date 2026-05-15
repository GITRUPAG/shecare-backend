package com.app.shecare.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class PeriodDatesRequest {

    private List<PeriodEntry> periods;
    private int numCycles = 3;

}