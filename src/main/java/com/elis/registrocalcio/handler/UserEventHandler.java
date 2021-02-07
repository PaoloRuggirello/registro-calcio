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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserEventHandler {

    @Autowired
    private UserEventRepository userEventRepository;
    @Autowired
    private EmailServiceImpl emailService;

    public UserEvent save(UserEvent userEvent) {
        return userEventRepository.save(userEvent);
    }

    public boolean isAlreadyRegistered(User user, Event toRegister) {
        List<UserEvent> userEventList = userEventRepository.findByUserAndPlayedIsFalseOrderByRegistrationTimeAsc(user); //Get all the events not played yet (ActiveEvent)
        if(userEventList.size() == 0)
            return false; //User haven't any registration
        Instant today = Instant.now();
        Instant nowPlus48Hours = today.plus(2, ChronoUnit.DAYS);
        return !(toRegister.getDate().isBefore(nowPlus48Hours) && toRegister.getDate().isAfter(today));
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
        int foundPlayers = userEventRepository.countByEventIdAndUsernameIn(eventId, allPlayers);
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
}
