package com.app.shecare.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PeriodLogRequest {

    private LocalDate startDate;

    private LocalDate endDate;

}