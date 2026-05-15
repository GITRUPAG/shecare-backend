package com.app.shecare.service;

import com.app.shecare.entity.Plan;
import com.app.shecare.repository.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    // Get all active plans
    public List<Plan> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .filter(Plan::isActive)
                .toList();
    }

    public Plan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
    }
}