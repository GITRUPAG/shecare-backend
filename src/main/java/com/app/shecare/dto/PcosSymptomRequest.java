package com.app.shecare.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PcosSymptomRequest {

    // 0 = No, 1 = Yes
    private int weightGain;       // Have you gained weight recently?
    private int hairGrowth;       // Excess facial/body hair?
    private int skinDarkening;    // Skin darkening (neck, underarms)?
    private int pimples;          // Frequent pimples/acne?

    // Optional — for more accurate prediction
    private Double amh;           // AMH level (if known)
    private Double fsh;           // FSH level (if known)
    private Double lh;            // LH level (if known)
    private Double waistHipRatio; // Waist-to-hip ratio (if known)
}