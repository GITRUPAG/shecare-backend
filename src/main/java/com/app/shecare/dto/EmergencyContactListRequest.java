package com.app.shecare.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class EmergencyContactListRequest {

    @NotEmpty(message = "At least one emergency contact is required")
    @Valid
    private List<EmergencyContactRequest> contacts;
}