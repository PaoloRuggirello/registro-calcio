package com.elis.registrocalcio.repository.general;

import com.elis.registrocalcio.model.general.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public interface EventRepository extends JpaRepository <Event, Long> {

    @Query("select e from Event e where e.date >= :day and e.date < :nextDay")
    List<Event> findEventInSameDateByDay(Instant day, Instant nextDay);

    //TODO sistemare query
    @Query("select e from Event e where e.id not in :ids and e.played = false order by e.date asc")
    List<Event> findByIdNotIn(List<Long> ids);

    List<Event> findAllByPlayedIsTrue();

    List<Event> findAllByPlayedIsFalseOrderByDateAsc();

    @Transactional
    @Modifying
    @Query("update Event e set e.played = true where e.played = false and e.date < :now")
    void updateEvents(Instant now);
}
