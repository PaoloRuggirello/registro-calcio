package com.example.registrocalcio.controller;

import com.example.registrocalcio.dto.EventDTO;
import com.example.registrocalcio.enumPackage.FootballRegisterException;
import com.example.registrocalcio.enumPackage.Role;
import com.example.registrocalcio.handler.EventHandler;
import com.example.registrocalcio.handler.UserHandler;
import com.example.registrocalcio.model.Event;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLIntegrityConstraintViolationException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    UserHandler userHandler;

    @Autowired
    EventHandler eventHandler;

    @Autowired
    EventRepository eventRepository;

    @PostMapping("/create")
    public EventDTO createEvent(@RequestBody EventDTO eventToCreate) throws SQLIntegrityConstraintViolationException {
        System.out.println(eventToCreate);
        if(!userHandler.hasUserPermissions(Role.ADMIN, eventToCreate.getCreator().getUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,FootballRegisterException.PERMISSION_DENIED.toString());
        if(!eventHandler.isEventValid(eventToCreate))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FootballRegisterException.EVENT_ALREADY_EXIST_IN_THE_GIVEN_DAY.toString());
        User creator = userHandler.findUserByUsername(eventToCreate.getCreator().getUsername()).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, FootballRegisterException.INVALID_REGISTRATION_FIELDS.toString()));
        Event event = new Event(eventToCreate, creator);
        System.out.println("New event : " + event);
        return new EventDTO(eventRepository.save(event));
    }


}
