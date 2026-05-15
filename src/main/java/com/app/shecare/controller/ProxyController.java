package com.app.shecare.controller;

import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.CaraClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ProxyController {

    private final CaraClientService caraClientService;

    public ProxyController(CaraClientService caraClientService) {
        this.caraClientService = caraClientService;
    }

    // @GetMapping("/history")
    // public ResponseEntity<?> history(
    //         @AuthenticationPrincipal CustomUserDetails userDetails,
    //         @RequestHeader("Authorization") String authHeader) {
    //     // Filter already validated this token — just strip "Bearer " and forward
    //     return ResponseEntity.ok(
    //         caraClientService.getHistory(authHeader.substring(7))
    //     );
    // }

    // @GetMapping("/memories")
    // public ResponseEntity<?> memories(
    //         @AuthenticationPrincipal CustomUserDetails userDetails,
    //         @RequestHeader("Authorization") String authHeader) {
    //     return ResponseEntity.ok(
    //         caraClientService.getMemories(authHeader.substring(7))
    //     );
    // }
}