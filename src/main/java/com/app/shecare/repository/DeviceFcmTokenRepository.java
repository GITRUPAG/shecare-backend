package com.app.shecare.repository;

import com.app.shecare.entity.DeviceFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface DeviceFcmTokenRepository extends JpaRepository<DeviceFcmToken, Long> {

    List<DeviceFcmToken> findByUserId(Long userId);

    Optional<DeviceFcmToken> findByFcmToken(String fcmToken);

    void deleteByFcmToken(String fcmToken);

    void deleteByUserId(Long userId);
}