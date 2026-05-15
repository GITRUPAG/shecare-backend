package com.app.shecare.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmergencyContactResponse {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private Boolean isPrimary;
    private Boolean isActive;
    private LocalDateTime createdAt;
}