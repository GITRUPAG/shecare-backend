package com.app.shecare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.shecare.entity.PaymentLinkRecord;

public interface PaymentLinkRepository extends JpaRepository<PaymentLinkRecord, Long> {

    Optional<PaymentLinkRecord> findTopByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, String status
    );

     Optional<PaymentLinkRecord>
    findByReferenceId(String referenceId);
}