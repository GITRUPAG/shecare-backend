package com.app.shecare.repository;

import com.app.shecare.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findByUserIdAndIsActiveTrue(Long userId);

    Optional<EmergencyContact> findByIdAndUserId(Long id, Long userId);

    int countByUserIdAndIsActiveTrue(Long userId);

    boolean existsByUserIdAndIsActiveTrue(Long userId);
}