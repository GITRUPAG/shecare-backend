package com.app.shecare.dto;

import lombok.Data;
import java.util.List;

@Data
public class PeriodPredictionRequest {

    private List<String> periodDates;

}