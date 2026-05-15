package com.app.shecare.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.shecare.entity.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}
