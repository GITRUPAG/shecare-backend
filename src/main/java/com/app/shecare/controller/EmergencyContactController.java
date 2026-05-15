package com.app.shecare.controller;

import com.app.shecare.dto.EmergencyContactListRequest;
import com.app.shecare.dto.EmergencyContactRequest;
import com.app.shecare.dto.EmergencyContactResponse;
import com.app.shecare.service.EmergencyContactService;
import com.app.shecare.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency-contacts")
@RequiredArgsConstructor
public class EmergencyContactController {

    private final EmergencyContactService service;

    // Add multiple contacts
    @PostMapping("/bulk")
    public ResponseEntity<List<EmergencyContactResponse>> addContacts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EmergencyContactListRequest request) {
        return ResponseEntity.ok(
            service.saveContacts(userDetails.getId(), request)
        );
    }

    // Add single contact
    @PostMapping
    public ResponseEntity<EmergencyContactResponse> addContact(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EmergencyContactRequest request) {
        return ResponseEntity.ok(
            service.addContact(userDetails.getId(), request)
        );
    }

    // Get all contacts
    @GetMapping
    public ResponseEntity<List<EmergencyContactResponse>> getContacts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
            service.getContacts(userDetails.getId())
        );
    }

    // Update a contact
    @PutMapping("/{contactId}")
    public ResponseEntity<EmergencyContactResponse> updateContact(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contactId,
            @Valid @RequestBody EmergencyContactRequest request) {
        return ResponseEntity.ok(
            service.updateContact(userDetails.getId(), contactId, request)
        );
    }

    // Delete a contact
    @DeleteMapping("/{contactId}")
    public ResponseEntity<String> deleteContact(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contactId) {
        service.deleteContact(userDetails.getId(), contactId);
        return ResponseEntity.ok("Contact deleted successfully");
    }
}