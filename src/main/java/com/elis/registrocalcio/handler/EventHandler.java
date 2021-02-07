package com.elis.registrocalcio.handler;

import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.enumPackage.Team;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.general.UserEvent;
import com.elis.registrocalcio.other.EmailServiceImpl;
import com.elis.registrocalcio.repository.general.EventRepository;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.repository.general.UserEventRepository;
import com.elis.registrocalcio.repository.general.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class EventHandler {
    @Autowired
    EventRepository eventRepository;
    @Autowired
    UserEventRepository userEventRepository;
    @Autowired
    EmailServiceImpl emailService;
    @Autowired
    UserRepository userRepository;

    public boolean isEventValid(EventDTO event) throws SQLIntegrityConstraintViolationException {
        if(!areFieldsValid(event))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FootballRegisterException.INVALID_REGISTRATION_FIELDS.toString());
        return isAloneInDay(event);
    }
    public boolean areFieldsValid(EventDTO event){
        return validateEventCategory(event.getCategory()) && validateDate(event.getDate());
    }

    public Event findEventByIdCheckOptional(Long id){
        Optional<Event> eventOptional = eventRepository.findById(id);
        if(eventOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FootballRegisterException.EVENT_NOT_FOUND.toString());
        return eventOptional.get();
    }

    private boolean validateEventCategory(String category){
        return !StringUtils.isBlank(category) && !ObjectUtils.isEmpty(Category.getCategoryFromString(category));
    }
    private boolean validateDate(Instant date){
        Instant endOfToday = LocalDate.ofInstant(Instant.now().plus(1, ChronoUnit.DAYS), ZoneId.of("UTC")).atStartOfDay().atZone(ZoneId.of("UTC")).toInstant();
        return !ObjectUtils.isEmpty(date) && endOfToday.isBefore(date); // Can't create event in the givenDay, admin should do that almost 1 DAY before the event
    }

    private boolean isAloneInDay(EventDTO event) {
        boolean isEventAlone = true;
        Instant date = event.getDate();
        LocalDateTime startDay = LocalDate.ofInstant(date, ZoneId.of("UTC")).atStartOfDay();
        LocalDateTime nextDay = startDay.plusDays(1);
        Instant startDayAsInstant = startDay.atZone(ZoneId.of("UTC")).toInstant();
        Instant nextDayAsInstant = nextDay.atZone(ZoneId.of("UTC")).toInstant();
        List<Event> eventsInDay = eventRepository.findEventInSameDateByDay(startDayAsInstant, nextDayAsInstant);
        for (Event eventInDB : eventsInDay)
            if(eventInDB.getCategory().equals(Category.getCategoryFromString(event.getCategory()))){
                isEventAlone = false;
                break;
            }
        return isEventAlone;
    }

    public void delete(Event event){
        eventRepository.delete(event);
    }

    public List<Event> findAll(){
        return eventRepository.findAll();
    }
    public List<Event> findActiveEvents(){
        return eventRepository.findAllByPlayedIsFalse();
    }
    public List<Event> findPastEvents(){
        return eventRepository.findAllByPlayedIsTrue();
    }
    public List<User> findEventPlayers(Long eventId){
        return userEventRepository.findPlayersOfEvent(eventId);
    }

    /**
     * This method send an email to each user that want to now the creation of a new Event
     * @param event
     */
    public void comunicateNewEventToUsers(Event event){
        List<String> mailList = userRepository.findNewsLetter();
        if(mailList.size() > 0)
            emailService.comunicateNewEventToMailList(mailList, event.getCategory().toString(), event.getDate());

    }
}
