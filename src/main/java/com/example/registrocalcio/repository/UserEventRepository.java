package com.example.registrocalcio.repository;

import com.example.registrocalcio.model.User;
import com.example.registrocalcio.model.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    @Query("select ue from UserEvent ue where ue.event.played = false and ue.user = :user order by ue.registrationTime asc")
    List<UserEvent> findByUserAndPlayedIsFalseOrderByRegistrationTimeAsc(User user);
}
