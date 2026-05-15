package com.app.shecare.repository;

import com.app.shecare.entity.Memory;
import com.app.shecare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemoryRepository extends JpaRepository<Memory, Long> {

    List<Memory> findByUser(User user);

    Optional<Memory> findByUserAndKey(User user, String key);
}