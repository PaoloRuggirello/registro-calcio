package com.elis.registrocalcio.repository.general;

import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.general.UserEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
    //Played = false -> eventDate > now
    //Played = true -> eventDate < now

    @Query("select ue from UserEvent ue where ue.event.date > :now and ue.user = :user and ue.event.category not in :freeCategories order by ue.registrationTime asc")
    List<UserEvent> findByUserAndPlayedIsFalseOrderByRegistrationTimeAsc(User user, Instant now, List<Category> freeCategories);

    @Query("select ue from UserEvent ue where ue.event.date = :start and ue.user = :user")
    List<UserEvent> findByUserWithStart(User user, Instant start);

    @Query("select ue.id from UserEvent ue where ue.user = :user and ue.event.date > :now")
    List<Long> findUserEventByDeletingUser(User user, Instant now);

    @Query("select ue.event from UserEvent ue where ue.event.date > :limitDate and ue.user.username = :username order by ue.event.date asc")
    List<Event> findEventsSubscribedByUser(String username, Instant limitDate);

    boolean existsByUserAndEvent(User user, Event event);

    @Transactional
    @Modifying
    @Query("delete from UserEvent ue where ue.id in :userEventId")
    void deleteByUserEventId(List<Long> userEventId);

    void deleteByEvent(Event event);

    void deleteByUserAndEvent(User user, Event event);

    List<UserEvent> findByUser(User user);

    @Query("select ue from UserEvent ue where ue.event.id = :eventId order by ue.registrationTime asc")
    List<UserEvent> findPlayersOfEvent(Long eventId, Pageable pageable);

    @Query("select ue from UserEvent ue where ue.event.id = :eventId and ue.user.username in :usernames")
    List<UserEvent> findByEventIdAndUsernameIn(Long eventId, List<String> usernames);

    @Query("select count(ue) from UserEvent ue where ue.event.id = :eventId and ue.user.username in :usernames")
    int countByEventIdAndUsernameIn(Long eventId, List<String> usernames);

    @Query("select ue from UserEvent ue where ue.event.id = :eventId")
    List<UserEvent> eventPlayers(Long eventId, Pageable pageable);
}
