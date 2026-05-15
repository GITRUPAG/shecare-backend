package com.app.shecare.controller;

import com.app.shecare.entity.User;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.VoiceService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VoiceController {

    private final VoiceService voiceService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> stream(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "conversationId", required = false) String conversationId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader("Authorization") String authHeader
    ) {

        User user = userDetails.getUser();
        String token = authHeader.substring(7);

        StreamingResponseBody stream = outputStream -> {
            voiceService.proxyStream(
                    audio,
                    conversationId,
                    token,
                    user,
                    outputStream
            );
        };

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(stream);
    }
}