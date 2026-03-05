package com.app.shecare.controller;

import com.app.shecare.dto.ai.PeriodDatesRequest;
import com.app.shecare.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/predict-period")
    public Object predictPeriod(@RequestBody PeriodDatesRequest request) {

        return aiService.predictFromDates(request);
    }

}