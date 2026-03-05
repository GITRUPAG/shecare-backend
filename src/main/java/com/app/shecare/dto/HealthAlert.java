package com.app.shecare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HealthAlert {

    private String type;

    private String message;

    private String severity;

}