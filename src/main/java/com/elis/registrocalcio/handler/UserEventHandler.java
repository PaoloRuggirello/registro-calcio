package com.elis.registrocalcio.handler;

import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.enumPackage.ChangeType;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.enumPackage.Team;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.general.UserEvent;
import com.elis.registrocalcio.other.EmailServiceImpl;
import com.elis.registrocalcio.repository.general.UserEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserEventHandler {

    @Autowired
    private UserEventRepository userEventRepository;
    @Autowired
    private EmailServiceImpl emailService;
    @Autowired
    private EventHandler eventHandler;

    public UserEvent save(UserEvent userEvent) {
        return userEventRepository.save(userEvent);
    }

    public boolean isAlreadyRegistered(User user, Event toRegister) {
        return userEventRepository.existsByUserAndEvent(user, toRegister); //Check if user is already registered to this event
     }

     public boolean hasActiveEvents(User user){
         return userEventRepository.findByUserAndPlayedIsFalseOrderByRegistrationTimeAsc(user, Instant.now()).size() > 0; //Get all the events not played yet (ActiveEvent)
     }

    public void deleteByUser(User toRemove){
        userEventRepository.deleteByUserEventId(userEventRepository.findUserEventByDeletingUser(toRemove, Instant.now()));
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

    public void verifyPlayers(Long eventId, List<String> team1, List<String> team2){
        List<String> allPlayers = new ArrayList<>();
        allPlayers.addAll(team1);
        allPlayers.addAll(team2);
        int maxPlayers = eventHandler.findEventByIdCheckOptional(eventId).getCategory().numberOfAllowedPlayers();
        int foundPlayers = userEventRepository.eventPlayers(eventId, PageRequest.of(0, maxPlayers)).size();
        if(foundPlayers != team1.size() + team2.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FootballRegisterException.WRONG_PLAYERS_ERROR.toString());
    }

    public void setTeam(Long eventId, List<String> users, Team team){
        List<UserEvent> players = userEventRepository.findByEventIdAndUsernameIn(eventId, users);
        players.forEach(player -> player.setTeam(team));
        userEventRepository.saveAll(players);
        Category category = players.get(0).getEvent().getCategory();
        Instant eventDate = players.get(0).getEvent().getDate();
        List<String> mailList = players.stream().map(userEvent -> userEvent.getUser().getEmail()).collect(Collectors.toList());
        if(mailList.size() > 0)
            emailService.communicateTeamToMailList(mailList, team.toString(), category.toString(), eventDate);
    }

    public List<Event> findEventsSubscribedByUser(String username){
        return userEventRepository.findEventsSubscribedByUser(username, Instant.now());
    }

    public void notifyChange(Event oldEvent, ChangeType changeType, Event newEvent){
        List<List<String>> mailList = new ArrayList<>();
        boolean end = false;
        int page = 0;
        while(!end){
            List<String> current = userEventRepository.findPlayersOfEvent(oldEvent.getId(), PageRequest.of(page, 25)).stream().map(ue -> ue.getUser().getEmail()).collect(Collectors.toList());
            if(current.size() > 0){
                page++;
                mailList.add(current);
            } else {
                end = true;
            }
        }
        mailList.forEach(list -> emailService.communicateChangeToMailList(list, changeType, oldEvent.getCategory().toString(), oldEvent.getDate(), newEvent));
    }
}
