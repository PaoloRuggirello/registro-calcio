package com.example.registrocalcio.controller;

import com.example.registrocalcio.dto.EventDTO;
import com.example.registrocalcio.dto.UserDTO;
import com.example.registrocalcio.enumPackage.FootballRegisterException;
import com.example.registrocalcio.enumPackage.Role;
import com.example.registrocalcio.handler.EventHandler;
import com.example.registrocalcio.handler.UserEventHandler;
import com.example.registrocalcio.handler.UserHandler;
import com.example.registrocalcio.model.Event;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.sql.SQLIntegrityConstraintViolationException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    UserHandler userHandler;

    @Autowired
    EventHandler eventHandler;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserEventHandler userEventHandler;

    @PostMapping("/create")
    public EventDTO createEvent(@RequestBody EventDTO eventToCreate) throws SQLIntegrityConstraintViolationException {
        System.out.println(eventToCreate);
        User creator = userHandler.findUserByUsernameCheckOptional(eventToCreate.getCreator().getUsername());
        userHandler.hasUserPermissions(Role.ADMIN, creator.getRole());
        if(!eventHandler.isEventValid(eventToCreate))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FootballRegisterException.EVENT_ALREADY_EXIST_IN_THE_GIVEN_DAY.toString());
        Event event = new Event(eventToCreate, creator);
        return new EventDTO(eventRepository.save(event));
    }

    @Transactional
    @PostMapping("/delete/{eventId}")
    public EventDTO deleteEvent(@PathVariable("eventId")Long eventId, @RequestBody UserDTO inCharge){
        User employee = userHandler.findUserByUsernameCheckOptional(inCharge.getUsername());
        userHandler.hasUserPermissions(Role.ADMIN, employee.getRole());
        Event toDelete = eventHandler.findEventByIdCheckOptional(eventId);
        EventDTO toDeleteEvent = new EventDTO(toDelete);
        userEventHandler.deleteByEvent(toDelete);
        eventHandler.delete(toDelete);
        return toDeleteEvent;
    }


}
