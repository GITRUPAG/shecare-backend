package com.app.shecare.controller;

import com.app.shecare.entity.Plan;
import com.app.shecare.service.PlanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public List<Plan> getPlans() {
        return planService.getAllPlans();
    }
}