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

    @Query("select e from Event e where e.id not in :ids and e.date > :date order by e.date asc")
    List<Event> findByIdNotIn(List<Long> ids, Instant date);

    @Query("select e from Event e where e.date < :now order by e.date desc")
    List<Event> findAllByPlayedIsTrue(Instant now);

    @Query("select e from Event e where e.date > :date order by e.date asc")
    List<Event> findAllByPlayedIsFalseOrderByDateAsc(Instant date);
}
