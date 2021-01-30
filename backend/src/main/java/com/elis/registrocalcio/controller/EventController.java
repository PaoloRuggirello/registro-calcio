package com.elis.registrocalcio.controller;

import com.elis.registrocalcio.handler.UserEventHandler;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.Event;
import com.elis.registrocalcio.repository.EventRepository;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.dto.UserDTO;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.EventHandler;
import com.elis.registrocalcio.model.User;
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

    @GetMapping("/find")
    public List<EventDTO> findAll(){
        return eventHandler.findAll().stream().map(EventDTO::new).collect(Collectors.toList());
    }
    @GetMapping("/find/{eventId}")
    public EventDTO findEvent(@PathVariable("eventId") Long eventId){
        return new EventDTO(eventHandler.findEventByIdCheckOptional(eventId));
    }
    @GetMapping("/findActive")
    public List<EventDTO> findActiveEvents(){
        return eventHandler.findActiveEvents().stream().map(EventDTO::new).collect(Collectors.toList());
    }
    @GetMapping("/findPast")
    public List<EventDTO> findPastEvents(){
        return eventHandler.findPastEvents().stream().map(EventDTO::new).collect(Collectors.toList());
    }


}
