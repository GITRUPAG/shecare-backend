package com.app.shecare.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "device_fcm_tokens",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "fcm_token"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceFcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Matches User.id which is Long
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;
}