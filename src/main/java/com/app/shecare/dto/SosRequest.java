package com.app.shecare.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SosRequest {

   

    
  // SosRequest.java — remove @NotNull, keep range validation optional
@DecimalMin(value = "-90.0",  inclusive = true, message = "Invalid latitude")
@DecimalMax(value = "90.0",   inclusive = true, message = "Invalid latitude")
private Double latitude;   // no @NotNull ✅

@DecimalMin(value = "-180.0", inclusive = true, message = "Invalid longitude")
@DecimalMax(value = "180.0",  inclusive = true, message = "Invalid longitude")
private Double longitude;  // no @NotNull ✅

    private Integer batteryLevel;       // phone charge %

    private String voiceMessage;        // what she spoke — "I am in danger"

    private String timestamp;
}