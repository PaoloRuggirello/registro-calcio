package com.elis.registrocalcio.controller;

import com.elis.registrocalcio.dto.PlayerDTO;
import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.enumPackage.Team;
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
import org.springframework.web.bind.annotation.RequestHeader;
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
    public EventDTO createEvent(@RequestBody EventDTO eventToCreate, @RequestHeader("Authorization") Token userToken) throws SQLIntegrityConstraintViolationException {
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
    public EventDTO deleteEvent(@PathVariable("eventId")Long eventId, @RequestHeader("Authorization") Token userToken){
        tokenHandler.checkToken(userToken, Role.ADMIN);
        Event toDelete = eventHandler.findEventByIdCheckOptional(eventId);
        if(toDelete.getPlayed()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, FootballRegisterException.CANNOT_DELETE_PLAYED_EVENTS.toString());
        EventDTO toDeleteEvent = new EventDTO(toDelete);
        userEventHandler.deleteByEvent(toDelete);
        eventHandler.delete(toDelete);
        return toDeleteEvent;
    }

    @GetMapping("/find")
    public List<EventDTO> findAll(@RequestHeader("Authorization") Token userToken){
        tokenHandler.checkToken(userToken);
        return eventHandler.findAll().stream().map(EventDTO::new).collect(Collectors.toList());
    }
    @GetMapping("/find/{eventId}")
    public EventDTO findEvent(@PathVariable("eventId") Long eventId, @RequestHeader("Authorization") Token userToken){
        tokenHandler.checkToken(userToken);
        return new EventDTO(eventHandler.findEventByIdCheckOptional(eventId));
    }
    @GetMapping("/findActive")
    public List<EventDTO> findActiveEvents(@RequestHeader("Authorization") Token userToken){
        tokenHandler.checkToken(userToken);
        return eventHandler.findActiveEvents().stream().map(EventDTO::new).collect(Collectors.toList());
    }
    @GetMapping("/findPast")
    public List<EventDTO> findPastEvents(@RequestHeader("Authorization") Token userToken){
        tokenHandler.checkToken(userToken);
        return eventHandler.findPastEvents().stream().map(EventDTO::new).collect(Collectors.toList());
    }
    @GetMapping("/findPlayers/{eventId}")
    public List<PlayerDTO> findPlayers(@PathVariable("eventId") Long eventId, @RequestHeader("Authorization") Token userToken){
        tokenHandler.checkToken(userToken);
        return eventHandler.findEventPlayers(eventId).stream().map(PlayerDTO::new).collect(Collectors.toList());
    }

    @PostMapping("/setTeam/{eventId}")
    public String setTeam(@PathVariable("eventId") Long eventId, @RequestBody List<String> blackTeam, @RequestBody List<String> whiteTeam, @RequestHeader("Authorization") Token token){
        tokenHandler.checkToken(token, Role.ADMIN);
        if(blackTeam.size() != whiteTeam.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.WRONG_TEAM_SIZE.toString());
        userEventHandler.verifyPlayers(eventId, blackTeam, whiteTeam);
        userEventHandler.setTeam(eventId, blackTeam, Team.BLACK);
        userEventHandler.setTeam(eventId, whiteTeam, Team.WHITE);
        return "Success";
    }
}
