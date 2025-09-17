package com.firefly.ragdemo.service;

import com.firefly.ragdemo.entity.User;
import com.firefly.ragdemo.mapper.UserRepository;
import com.firefly.ragdemo.secutiry.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return new CustomUserPrincipal(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByUserId(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent() && user.get().getIsActive()) {
            return new CustomUserPrincipal(user.get());
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void updateLastLogin(String userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
    }
}
