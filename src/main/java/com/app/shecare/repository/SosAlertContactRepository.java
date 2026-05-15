package com.app.shecare.repository;

import com.app.shecare.entity.SosAlertContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SosAlertContactRepository extends JpaRepository<SosAlertContact, Long> {
}