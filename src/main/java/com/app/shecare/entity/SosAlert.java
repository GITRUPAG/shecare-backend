package com.app.shecare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sos_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SosAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Location
    @Column(nullable = true)  // ✅ allow null at JPA level
private Double latitude;

@Column(nullable = true)  // ✅ allow null at JPA level
private Double longitude;

    private String locationAddress;     // reverse geocoded readable address

    // Device Info
    private Integer batteryLevel;       // phone charge %

    // What she spoke
    @Column(columnDefinition = "TEXT")
    private String voiceMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SosStatus status;

    @CreationTimestamp
    private LocalDateTime triggeredAt;

    @OneToMany(mappedBy = "sosAlert", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<SosAlertContact> notifiedContacts;
}