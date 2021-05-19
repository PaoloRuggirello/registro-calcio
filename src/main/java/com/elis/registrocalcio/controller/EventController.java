package com.elis.registrocalcio.controller;

import com.elis.registrocalcio.dto.PlayerDTO;
import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.enumPackage.ChangeType;
import com.elis.registrocalcio.enumPackage.Team;
import com.elis.registrocalcio.handler.TokenHandler;
import com.elis.registrocalcio.handler.UserEventHandler;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.security.SecurityToken;
import com.elis.registrocalcio.other.ExceptionUtils;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.EventHandler;
import com.elis.registrocalcio.model.general.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.Instant;
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
    UserEventHandler userEventHandler;
    @Autowired
    TokenHandler tokenHandler;
    private static Logger log = LogManager.getLogger(EventController.class);

    @PostMapping("/create")
    public EventDTO createEvent(@RequestBody EventDTO eventToCreate, @RequestHeader("Authorization") Token userToken) {
        SecurityToken token = tokenHandler.checkToken(userToken, Role.ADMIN);
        log.info("{} is creating an event. Event info: {}", token.getUsername(), eventToCreate);
        User creator = userHandler.findUserByUsernameCheckOptional(token.getUsername());
        if(!eventHandler.areFieldsValid(eventToCreate))
            ExceptionUtils.throwResponseStatus(EventController.class, HttpStatus.FORBIDDEN, FootballRegisterException.INVALID_REGISTRATION_FIELDS, " Error during event creation. Creator: "+ token.getUsername() +" Event info: "+ eventToCreate);
        Event event = new Event(eventToCreate, creator);
        EventDTO toReturn = new EventDTO(eventHandler.save(event));
        eventHandler.newEventToNewsLetter(event);
        log.info("Event {} created by {}", toReturn, token.getUsername());
        return toReturn;
    }

    @Transactional
    @PostMapping("/delete/{eventId}")
    public EventDTO deleteEvent(@PathVariable("eventId")Long eventId, @RequestHeader("Authorization") Token userToken){
        String username = tokenHandler.checkToken(userToken, Role.ADMIN).getUsername();
        log.info("{} is Trying to delete event. EventId {}", username, eventId);
        Event toDelete = eventHandler.findEventByIdCheckOptional(eventId);
        if(toDelete.getPlayed() || toDelete.getDate().isBefore(Instant.now())) ExceptionUtils.throwResponseStatus(this.getClass(), HttpStatus.FORBIDDEN, FootballRegisterException.CANNOT_DELETE_PLAYED_EVENTS, "Error while deleting event with id :" + eventId + " by user " + username);
        EventDTO toDeleteEvent = new EventDTO(toDelete);
        userEventHandler.notifyChange(toDelete, ChangeType.DELETE, null);
        userEventHandler.deleteByEvent(toDelete);
        eventHandler.delete(toDelete);
        log.info("Event with id {} deleted by {}", eventId, username);
        return toDeleteEvent;
    }

    @PostMapping("/modify")
    public ResponseEntity<String> modifyEvent(@RequestBody EventDTO modifiedEvent, @RequestHeader("Authorization") Token userToken){
        String username = tokenHandler.checkToken(userToken, Role.ADMIN).getUsername();
        log.info("{} is Trying to modify event with id {}. New fields are: {}", username, modifiedEvent.getId(), modifiedEvent);
        Event toModify = eventHandler.findEventByIdCheckOptional(modifiedEvent.getId());
        userEventHandler.notifyChange(toModify, ChangeType.MODIFY, new Event(modifiedEvent));
        toModify.updateFieldsFromDTO(modifiedEvent);
        log.info("Event with id {} correctly modified by {}", modifiedEvent.getId(), username);
        eventHandler.save(toModify);

        return new ResponseEntity<>(HttpStatus.OK);
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
        String username = tokenHandler.checkToken(userToken).getUsername();
        return eventHandler.findActiveEvents(username).stream().map(EventDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/findPast")
    public List<EventDTO> findPastEvents(@RequestHeader("Authorization") Token userToken){
        tokenHandler.checkToken(userToken);
        return eventHandler.findPastEvents().stream().map(EventDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/findPlayers/{eventId}/{page}")
    public List<PlayerDTO> findPlayers(@PathVariable("eventId") Long eventId, @PathVariable("page") Integer page, @RequestHeader("Authorization") Token userToken){
        tokenHandler.checkToken(userToken);
        return eventHandler.findEventPlayers(eventId, page).stream().map(PlayerDTO::new).collect(Collectors.toList());
    }

    @PostMapping("/setTeam/{eventId}")
    public ResponseEntity<String> setTeam(@PathVariable("eventId") Long eventId, @RequestParam("blackTeam") List<String> blackTeam, @RequestParam("whiteTeam") List<String> whiteTeam, @RequestHeader("Authorization") Token token){
        String username = tokenHandler.checkToken(token, Role.ADMIN).getUsername();
        log.info("{} is setting-up teams for event: {}.\nBLACK TEAM: {} \nWHITE TEAM: {}", username, eventId, blackTeam, whiteTeam);
        if(!eventHandler.isTeamsSizeValid(blackTeam.size(), whiteTeam.size()))
            ExceptionUtils.throwResponseStatus(this.getClass(), HttpStatus.BAD_REQUEST, FootballRegisterException.WRONG_TEAM_SIZE, " Error while setting up teams. Operated by " + username +" on event "+ eventId + " Teams - Black:"+ blackTeam + " white " + whiteTeam);
        userEventHandler.verifyPlayers(eventId, blackTeam, whiteTeam);
        userEventHandler.setTeam(eventId, blackTeam, Team.BLACK);
        userEventHandler.setTeam(eventId, whiteTeam, Team.WHITE);
        log.info("Teams correctly set. Operated by {} Teams: Black {} White {}", username, blackTeam, whiteTeam);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/export/{eventId}")
    public ResponseEntity<InputStreamResource> exportEvent(@PathVariable("eventId") Long eventId) throws FileNotFoundException {
//        String username = tokenHandler.checkToken(token, Role.ADMIN).getUsername();
        String username = "paolo.ruggirello";
        log.info("{} is exporting event {}", username, eventId);
        Event toExport = eventHandler.findEventByIdCheckOptional(eventId);
        if(!toExport.getPlayed()) ExceptionUtils.throwResponseStatus(this.getClass(), HttpStatus.FORBIDDEN, FootballRegisterException.EVENT_NOT_PLAYED_YET, username + " is trying to download not played event: " + toExport);
        String filePath = "";
        String fileName = eventHandler.generateFileName(toExport);
        try {
            filePath = eventHandler.exportEvent(toExport, fileName);
        } catch (Exception e){
            ExceptionUtils.throwResponseStatus(this.getClass(), HttpStatus.INTERNAL_SERVER_ERROR, FootballRegisterException.CANNOT_EXPORT_FILE, " " + username + " - cannot export file relative to match :" + toExport + "\n" + e);
        }

        File match = new File(filePath);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(match));

        ContentDisposition contentDisposition = ContentDisposition.builder("inline")
                .filename(fileName)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(match.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
