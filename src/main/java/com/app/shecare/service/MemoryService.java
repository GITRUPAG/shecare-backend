package com.app.shecare.service;

import com.app.shecare.entity.Memory;
import com.app.shecare.entity.User;
import com.app.shecare.repository.MemoryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemoryService {

    private final MemoryRepository memoryRepository;

    public void upsertMemory(User user, String key, String value, double confidence) {

        Memory memory = memoryRepository
                .findByUserAndKey(user, key)
                .orElse(null);

        if (memory == null) {
            memory = Memory.builder()
                    .user(user)
                    .key(key)
                    .value(value)
                    .confidence(confidence)
                    .build();
        } else {
            memory.setValue(value);
            memory.setConfidence(confidence);
        }

        memoryRepository.save(memory);
    }

    public List<Memory> getUserMemories(User user) {
        return memoryRepository.findByUser(user);
    }

    // 🔥 NEW
    public void deleteMemory(Long id) {
        memoryRepository.deleteById(id);
    }
}