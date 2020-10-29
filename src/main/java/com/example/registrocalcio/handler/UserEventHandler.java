package com.example.registrocalcio.handler;

import com.example.registrocalcio.model.Event;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.model.UserEvent;
import com.example.registrocalcio.repository.UserEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserEventHandler {

    @Autowired
    private UserEventRepository userEventRepository;

    public UserEvent save(UserEvent userEvent) {
        return userEventRepository.save(userEvent);
    }

    public boolean isAlreadyRegistered(User user, Event event) {
        Optional<UserEvent> userEvent = userEventRepository.findByUserAndAndEvent(user, event);
        return userEvent.isPresent();
    }


}
