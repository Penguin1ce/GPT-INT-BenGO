package com.firefly.ragdemo.service;

import com.firefly.ragdemo.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {

    org.springframework.security.core.userdetails.UserDetails loadUserByUserId(String userId);

    Optional<User> findByUsername(String username);

    Optional<User> findById(String id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User save(User user);

    void updateLastLogin(String userId);
}
