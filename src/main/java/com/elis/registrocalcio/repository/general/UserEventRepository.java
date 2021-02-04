package com.elis.registrocalcio.repository.general;

import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.general.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    @Query("select ue from UserEvent ue where ue.event.played = false and ue.user = :user order by ue.registrationTime asc")
    List<UserEvent> findByUserAndPlayedIsFalseOrderByRegistrationTimeAsc(User user);

    @Query("select ue.id from UserEvent ue where ue.user = :user and ue.event.played = false")
    List<Long> findUserEventByDeletingUser(User user);

    @Transactional
    @Modifying
    @Query("delete from UserEvent ue where ue.id in :userEventId")
    void deleteByUserEventId(List<Long> userEventId);

    void deleteByEvent(Event event);

    void deleteByUserAndEvent(User user, Event event);

    List<UserEvent> findByUser(User user);

    @Query("select ue.user.username from UserEvent ue where ue.event.id = :eventId order by ue.registrationTime asc")
    List<String> findPlayersOfEvent(Long eventId);
}
