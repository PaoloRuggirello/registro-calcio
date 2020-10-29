package com.example.registrocalcio.repository;

import com.example.registrocalcio.model.Event;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.model.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    Optional<UserEvent> findByUserAndAndEvent(User user, Event event);
}
