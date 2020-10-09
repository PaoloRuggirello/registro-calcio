package com.example.registrocalcio.repository;

import com.example.registrocalcio.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository <Event, Long> {
}
