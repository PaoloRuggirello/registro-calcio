package com.elis.registrocalcio.repository.general;

import com.elis.registrocalcio.model.general.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long> {

    @Query("select u.role from User u where u.username = :username")
    Optional<String> findRoleByUsername(@Param("username")String username);

    Long countByNameAndSurnameIgnoreCaseAndIsActiveIsTrue(String name, String username);
    Optional<User> findByEmailAndIsActiveIsTrue(String email);
    Optional<User> findByUsernameAndIsActiveIsTrue(String username);
    List<User> findAllByIsActiveIsTrue();
}
