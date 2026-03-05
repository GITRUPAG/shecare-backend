package com.app.shecare.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BmiResponse {

    private double bmi;

    private String category;

}