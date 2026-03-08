package com.app.shecare.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.app.shecare.entity.Role;
import com.app.shecare.entity.Profile;

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
    private Profile profile;
}