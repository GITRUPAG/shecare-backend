package com.app.shecare.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class PcosPredictionRequest {

    private double age;
    private double bmi;
    private double cycle_length;
    private int cycle_regularity;

    private double follicle_no_right;
    private double follicle_no_left;

    private double amh;
    private double fsh;
    private double lh;
    private double fsh_lh_ratio;

    private double waist_hip_ratio;
    private double endometrium_mm;

    private double avg_follicle_size_r;
    private double avg_follicle_size_l;

    private int weight_gain;
    private int hair_growth;
    private int skin_darkening;
    private int pimples;
}