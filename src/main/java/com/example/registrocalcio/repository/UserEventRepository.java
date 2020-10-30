package com.example.registrocalcio.repository;

import com.example.registrocalcio.model.User;
import com.example.registrocalcio.model.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    List<UserEvent> findByUserAndPlayedIsFalseOrderByRegistrationTimeAsc(User user);
}
