package com.example.registrocalcio.repository;

import com.example.registrocalcio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long> {

    @Query("select u.role from User u where u.username = :username")
    Optional<String> findRoleByUsername(@Param("username")String username);

    Long countByNameAndSurnameIgnoreCase(String name, String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

}
