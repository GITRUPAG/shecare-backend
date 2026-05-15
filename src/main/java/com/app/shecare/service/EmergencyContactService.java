package com.app.shecare.service;

import com.app.shecare.dto.EmergencyContactListRequest;
import com.app.shecare.dto.EmergencyContactRequest;
import com.app.shecare.dto.EmergencyContactResponse;
import com.app.shecare.entity.EmergencyContact;
import com.app.shecare.entity.User;
import com.app.shecare.exception.ResourceNotFoundException;
import com.app.shecare.exception.ValidationException;
import com.app.shecare.repository.EmergencyContactRepository;
import com.app.shecare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmergencyContactService {

    private final EmergencyContactRepository contactRepo;
    private final UserRepository userRepo;

    // ─── Add Multiple Contacts ───────────────────────────────────────
    public List<EmergencyContactResponse> saveContacts(
            Long userId,
            EmergencyContactListRequest request) {

        User user = findUser(userId);

        List<EmergencyContactRequest> contacts = request.getContacts();

        // If none marked as primary → auto set first one
        boolean hasPrimary = contacts.stream()
                .anyMatch(c -> Boolean.TRUE.equals(c.getIsPrimary()));

        if (!hasPrimary) {
            contacts.get(0).setIsPrimary(true);
        }

        List<EmergencyContact> entities = contacts.stream()
                .map(req -> buildEntity(req, user))
                .collect(Collectors.toList());

        return contactRepo.saveAll(entities)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Add Single Contact ──────────────────────────────────────────
    public EmergencyContactResponse addContact(
            Long userId,
            EmergencyContactRequest request) {

        User user = findUser(userId);
        EmergencyContact contact = buildEntity(request, user);
        return toResponse(contactRepo.save(contact));
    }

    // ─── Get All Contacts ────────────────────────────────────────────
    public List<EmergencyContactResponse> getContacts(Long userId) {
        return contactRepo.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Delete Contact (soft delete, min 1 required) ────────────────
    public void deleteContact(Long userId, Long contactId) {

        int count = contactRepo.countByUserIdAndIsActiveTrue(userId);

        if (count <= 1) {
            throw new ValidationException(
                "Cannot delete — at least one emergency contact is required"
            );
        }

        EmergencyContact contact = contactRepo
                .findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Contact not found"
                ));

        contact.setIsActive(false);   // soft delete
        contactRepo.save(contact);
    }

    // ─── Update Contact ──────────────────────────────────────────────
    public EmergencyContactResponse updateContact(
            Long userId,
            Long contactId,
            EmergencyContactRequest request) {

        EmergencyContact contact = contactRepo
                .findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Contact not found"
                ));

        contact.setName(request.getName());
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setIsPrimary(request.getIsPrimary());

        return toResponse(contactRepo.save(contact));
    }

    // ─── Helpers ─────────────────────────────────────────────────────
    private User findUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with id: " + userId
                ));
    }

    private EmergencyContact buildEntity(EmergencyContactRequest req, User user) {
        return EmergencyContact.builder()
                .user(user)
                .name(req.getName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .isPrimary(Boolean.TRUE.equals(req.getIsPrimary()))
                .isActive(true)
                .build();
    }

    private EmergencyContactResponse toResponse(EmergencyContact c) {
        return EmergencyContactResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .isPrimary(c.getIsPrimary())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}