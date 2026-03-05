package com.app.shecare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "period_predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate predictedStartDate;

    private LocalDate predictedEndDate;

    private LocalDate ovulationDay;

    private LocalDate fertileStart;

    private LocalDate fertileEnd;

    private Double predictedCycleLength;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}