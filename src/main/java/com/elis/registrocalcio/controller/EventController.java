package com.elis.registrocalcio.controller;

import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.handler.TokenHandler;
import com.elis.registrocalcio.handler.UserEventHandler;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.repository.general.EventRepository;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.EventHandler;
import com.elis.registrocalcio.model.general.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    TokenHandler tokenHandler;

    @PostMapping("/create")
    public EventDTO createEvent(@RequestBody EventDTO eventToCreate, @RequestBody Token userToken) throws SQLIntegrityConstraintViolationException {
        tokenHandler.checkIfAreTheSameUser(userToken, eventToCreate.getCreator().getUsername(), Role.ADMIN);
        System.out.println(eventToCreate);
        User creator = userHandler.findUserByUsernameCheckOptional(eventToCreate.getCreator().getUsername());
        if(!eventHandler.isEventValid(eventToCreate))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FootballRegisterException.EVENT_ALREADY_EXIST_IN_THE_GIVEN_DAY.toString());
        Event event = new Event(eventToCreate, creator);
        eventHandler.comunicateNewEventToUsers(event);
        return new EventDTO(eventRepository.save(event));
    }

    @Transactional
    @PostMapping("/delete/{eventId}")
    public EventDTO deleteEvent(@PathVariable("eventId")Long eventId, @RequestBody Token userToken){
        tokenHandler.checkToken(userToken, Role.ADMIN);
        Event toDelete = eventHandler.findEventByIdCheckOptional(eventId);
        EventDTO toDeleteEvent = new EventDTO(toDelete);
        userEventHandler.deleteByEvent(toDelete);
        eventHandler.delete(toDelete);
        return toDeleteEvent;
    }

    @GetMapping("/find")
    public List<EventDTO> findAll(@RequestBody Token userToken){
        tokenHandler.checkToken(userToken);
        return eventHandler.findAll().stream().map(EventDTO::new).collect(Collectors.toList());
    }
    @GetMapping("/find/{eventId}")
    public EventDTO findEvent(@PathVariable("eventId") Long eventId, @RequestBody Token userToken){
        tokenHandler.checkToken(userToken);
        return new EventDTO(eventHandler.findEventByIdCheckOptional(eventId));
    }
    @GetMapping("/findActive")
    public List<EventDTO> findActiveEvents(@RequestBody Token userToken){
        tokenHandler.checkToken(userToken);
        return eventHandler.findActiveEvents().stream().map(EventDTO::new).collect(Collectors.toList());
    }
    @GetMapping("/findPast")
    public List<EventDTO> findPastEvents(@RequestBody Token userToken){
        tokenHandler.checkToken(userToken);
        return eventHandler.findPastEvents().stream().map(EventDTO::new).collect(Collectors.toList());
    }
    @GetMapping("/findPlayers/{eventId}")
    public List<String> findPlayers(@PathVariable("eventId") Long eventId, @RequestBody Token userToken){
        tokenHandler.checkToken(userToken);
        return eventHandler.findEventPlayers(eventId);
    }
}
