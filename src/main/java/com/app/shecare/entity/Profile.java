package com.app.shecare.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Basic Information
    @Column(nullable = false)
    private String fullName;

    private LocalDate dateOfBirth;

    private String gender;

    private String city;
    private String state;
    private String country;

    private String profileImageUrl;

    // 🔹 Health Basics
    private Double height; // cm
    private Double weight; // kg
    private String bloodGroup;

    // 🔹 Lifestyle Info
    @Builder.Default
    private Boolean smoker = false;
    @Builder.Default
    private Boolean alcoholic = false;

    private String activityLevel; // LOW, MODERATE, HIGH

    // 🔹 Medical Info
    @Builder.Default
    private Boolean hasPCOS = false;
    @Builder.Default
    private Boolean hasDiabetes = false;
    @Builder.Default
    private Boolean hasThyroid = false;
    
    @Builder.Default
    private Boolean hasHypertension = false;

    // 🔹 Emergency Contact
    private String emergencyContactName;
    private String emergencyContactNumber;

    // PCOS symptom flags — 0 or 1
    private Integer pcosWeightGain;
    private Integer pcosHairGrowth;
    private Integer pcosSkinDarkening;
    private Integer pcosPimples;
    private Double  pcosAmh;
    private Double  pcosFsh;
    private Double  pcosLh;
    private Double  pcosWaistHipRatio;

    // 🔹 Preferences
    @Builder.Default
    @Column(nullable = false)
    private Boolean notificationsEnabled = true;
    @Builder.Default
    @Column(nullable = false)
    private boolean darkModeEnabled = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 🔹 Relationship with User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    // 🔹 Auto Timestamp
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}