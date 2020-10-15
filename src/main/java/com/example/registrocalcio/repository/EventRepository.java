package com.example.registrocalcio.repository;

import com.example.registrocalcio.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface EventRepository extends JpaRepository <Event, Long> {

    @Query("select e from Event e where e.date >= :day and e.date < :nextDay")
    List<Event> findEventInSameDateByDay(Instant day,Instant nextDay);
}
