package com.app.shecare.repository;

import com.app.shecare.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.app.shecare.entity.User;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    Optional<Profile> findByUser(User user);
}