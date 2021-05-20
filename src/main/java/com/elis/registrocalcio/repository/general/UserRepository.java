package com.elis.registrocalcio.repository.general;

import com.elis.registrocalcio.model.general.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long> {

    @Query("select u.role from User u where u.username = :username")
    Optional<String> findRoleByUsername(@Param("username")String username);

    @Query("select u.email from User u where u.isActive = true and u.newsLetter = true")
    List<String> findNewsLetter(Pageable pageable);

    Long countByNameAndSurnameIgnoreCaseAndIsActiveIsTrue(String name, String username);
    Optional<User> findByEmailAndIsActiveIsTrue(String email);

    @Query("select u from User u where u.isActive = true and (u.username = :username or u.email = :username)")
    Optional<User> findByUsernameAndIsActiveIsTrue(@Param("username") String username);

    @Query("select u from User u where u.isActive = true order by u.name, u.surname")
    List<User> findAllByIsActiveIsTrue();
}
