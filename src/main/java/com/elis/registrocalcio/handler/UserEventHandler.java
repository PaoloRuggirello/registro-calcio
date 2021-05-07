package com.elis.registrocalcio.handler;

import com.elis.registrocalcio.enumPackage.Category;
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

//    public boolean isAlreadyRegistered(User user, Event toRegister) {
//        if(userEventRepository.existsByUserAndId(user, toRegister.getId())) return true; //Check if user is already registered to this event
//        List<UserEvent> userEventList = userEventRepository.findByUserAndPlayedIsFalseOrderByRegistrationTimeDesc(user); //Get all the events not played yet (ActiveEvent)
//        if(userEventList.size() == 0)
//            return false; //User haven't any registration
//        UserEvent lastRegistered = userEventList.get(0); //List sorted by date desc, the first element is the last registration time
//        if(!DateUtils.areInTheSameWeek(lastRegistered.getEvent().getDate(), toRegister.getDate()))
//            return false; //If events aren't in the same week user can be registered to eachOther
//        Instant today = Instant.now();
//        Instant nowPlus48Hours = today.plus(2, ChronoUnit.DAYS);
//        return !(toRegister.getDate().isBefore(nowPlus48Hours) && toRegister.getDate().isAfter(today));//Check if the event is in the range today - next 48h, if yes return false <- means that user can subscribe the event
//    }

    public boolean isAlreadyRegistered(User user, Event toRegister) {
        if(userEventRepository.existsByUserAndId(user, toRegister.getId())) return true; //Check if user is already registered to this event
        List<UserEvent> userEventList = userEventRepository.findByUserAndPlayedIsFalseOrderByRegistrationTimeAsc(user); //Get all the events not played yet (ActiveEvent)
        userEventList = userEventList.stream().filter(ue -> ue.getEvent().getDate().isAfter(Instant.now())).collect(Collectors.toList()); //Double check about isPlayed value
        if(userEventList.size() > 0) return true; //User has not played events
        return false; //User hasn't any not played events
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
            emailService.comunicateTeamToMailList(mailList, team.toString(), category.toString(), eventDate);
    }

    public List<Event> findEventsSubscribedByUser(String username){
        return userEventRepository.findEventsSubscribedByUser(username);
    }
}
