package com.app.shecare.controller;

import com.app.shecare.dto.SosRequest;
import com.app.shecare.dto.SosResponse;
import com.app.shecare.entity.SosAlert;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.SosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sos")
@RequiredArgsConstructor
public class SosController {

    private final SosService sosService;

    @PostMapping("/trigger")
    public ResponseEntity<SosResponse> triggerSOS(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SosRequest request) {
        return ResponseEntity.ok(
            sosService.triggerSOS(userDetails.getId(), request)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<SosAlert>> getSosHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
            sosService.getSosHistory(userDetails.getId())
        );
    }
}