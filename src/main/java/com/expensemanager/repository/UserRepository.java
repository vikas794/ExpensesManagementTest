package com.expensemanager.repository;

import com.expensemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    // Adding existsByUsername and existsByEmail for registration validation
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
