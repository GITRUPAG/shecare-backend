package com.app.shecare.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.app.shecare.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.app.shecare.entity.Profile;
import java.util.ArrayList;
import java.util.List;
import com.app.shecare.entity.EmergencyContact;
import com.app.shecare.entity.DailySymptomLog;
import java.time.LocalDate;


@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = "phoneNumber")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
        private String username;

    // Login via Email or Phone
    @Column(nullable = false)
    private String email;

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "google_id")
private String googleId;

    @Column(nullable = false)
private Boolean enabled = true;

@Column(nullable = false)
private Boolean emailVerified = false;

@Column(nullable = false)
private Boolean phoneVerified = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Automatically set timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // One-to-One mapping with Profile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Profile profile;

    // Add inside User.java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
@JsonIgnore
private List<EmergencyContact> emergencyContacts = new ArrayList<>();

@OneToMany(mappedBy = "user")
@JsonIgnore
List<DailySymptomLog> symptomLogs = new ArrayList<>();

// 🔹 Premium Subscription
@Column(name = "is_premium")
private Boolean isPremium = false;

@Column(name = "premium_expiry")
private LocalDateTime premiumExpiry;

// 🔹 Chat Limit Tracking
@Column(name = "chat_count_today")
private Integer chatCountToday = 0;

@Column(name = "last_chat_date")
private LocalDate lastChatDate;

@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
@JsonIgnore
private List<Conversation> conversations = new ArrayList<>();

}