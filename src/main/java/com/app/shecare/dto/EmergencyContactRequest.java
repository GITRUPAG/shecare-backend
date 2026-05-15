package com.app.shecare.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EmergencyContactRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Invalid phone number")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;           // optional

    private Boolean isPrimary = false;
}