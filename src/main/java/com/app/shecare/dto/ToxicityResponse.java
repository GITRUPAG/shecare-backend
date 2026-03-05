package com.app.shecare.dto;

import lombok.Data;

@Data
public class ToxicityResponse {

    private boolean is_toxic;

    private double overall_score;

    private String severity;

    private String top_category;

    private String action;

    private String support_message;

}