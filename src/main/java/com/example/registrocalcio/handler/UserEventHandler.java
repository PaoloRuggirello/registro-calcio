package com.example.registrocalcio.handler;

import com.example.registrocalcio.model.Event;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.model.UserEvent;
import com.example.registrocalcio.repository.UserEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class UserEventHandler {

    @Autowired
    private UserEventRepository userEventRepository;

    public UserEvent save(UserEvent userEvent) {
        return userEventRepository.save(userEvent);
    }

    public boolean isAlreadyRegistered(User user, Event toRegister) {
        List<UserEvent> userEventList = userEventRepository.findByUserAndPlayedIsFalseOrderByRegistrationTimeAsc(user); //Get all the events not played yet (ActiveEvent)
        Event mostRecentEvent = toRegister;
        if(userEventList.size() == 0)
            return false; //User haven't any registration
        else {
            for(UserEvent temp : userEventList) {
                if(temp.getEvent().getDate().isBefore(toRegister.getDate()) && temp.getEvent().getDate().isBefore(mostRecentEvent.getDate()))
                    mostRecentEvent = temp.getEvent();
            }
        }
        Instant now = new Date().toInstant().atZone(ZoneId.of("UTC")).truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant nowPlus48Hours = now.plus(2, ChronoUnit.DAYS);
        return !mostRecentEvent.getDate().isBefore(nowPlus48Hours) || !mostRecentEvent.getDate().isAfter(now);
    }

    public void deleteByUser(User toRemove){
        userEventRepository.deleteByUserEventId(userEventRepository.findUserEventByDeletingUser(toRemove));
    }
    public void deleteByEvent(Event event){
        userEventRepository.deleteByEvent(event);
    }
    public void deleteByUserAndEvent(User toDelete, Event event){
        userEventRepository.deleteByUserAndEvent(toDelete, event);
    }

    public List<UserEvent> findByUser(User user){
        return userEventRepository.findByUser(user);
    }
}
