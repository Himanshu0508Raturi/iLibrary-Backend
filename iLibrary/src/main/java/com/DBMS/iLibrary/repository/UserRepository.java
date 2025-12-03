package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Optional custom query methods
    Optional<User> findByUsername(String username);
}
