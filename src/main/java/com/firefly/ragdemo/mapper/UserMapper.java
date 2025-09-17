package com.firefly.ragdemo.mapper;

import com.firefly.ragdemo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface UserMapper {

    Optional<User> findById(@Param("id") String id);

    Optional<User> findByUsername(@Param("username") String username);

    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByUsername(@Param("username") String username);

    boolean existsByEmail(@Param("email") String email);

    int insert(User user);

    int updateLastLogin(@Param("userId") String userId, @Param("lastLogin") LocalDateTime lastLogin);
} 