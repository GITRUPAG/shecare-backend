package com.app.shecare.controller;

import com.app.shecare.entity.Memory;
import com.app.shecare.entity.User;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.MemoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/memory")
@RequiredArgsConstructor
public class MemoryController {

    private final MemoryService memoryService;

    // 🔥 GET ALL USER MEMORIES
    @GetMapping
    public List<Memory> getMemories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        return memoryService.getUserMemories(user);
    }

    // 🔥 OPTIONAL: DELETE MEMORY
    @DeleteMapping("/{id}")
    public void deleteMemory(@PathVariable Long id) {
        memoryService.deleteMemory(id);
    }
}