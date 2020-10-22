package com.example.registrocalcio.repository;

import com.example.registrocalcio.model.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
}
