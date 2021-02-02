package com.elis.registrocalcio.handler;

import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.model.Event;
import com.elis.registrocalcio.repository.EventRepository;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class EventHandler {
    @Autowired
    EventRepository eventRepository;

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
    private boolean validateDate(Date date){
        Date today = new Date();
        System.out.println("Today : " + today);
        return !ObjectUtils.isEmpty(date) && today.before(date);
    }

    private boolean isAloneInDay(EventDTO event) {
        boolean isEventAlone = true;
        Date date = event.getDate();
        LocalDateTime startDay = LocalDate.ofInstant(date.toInstant(), ZoneId.of("UTC")).atStartOfDay();
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
}
