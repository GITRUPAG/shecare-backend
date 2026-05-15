package com.app.shecare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


@Data
@AllArgsConstructor
public class HealthAlert {

    private String type;

    private String message;

    private String severity;

    private LocalDate alertDate;

    public int getDaysUntil() {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), alertDate);
    }

}