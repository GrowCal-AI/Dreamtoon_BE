package com.dreamtoon.domain.user.repository;

import com.dreamtoon.domain.user.entity.SocialProvider;
import com.dreamtoon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);
    
    boolean existsByEmail(String email);
}
