package com.poc.sync.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poc.sync.model.User;

import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
   Optional<User> findByUsername(String username);
}