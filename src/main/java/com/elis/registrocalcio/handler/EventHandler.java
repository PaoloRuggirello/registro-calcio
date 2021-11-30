package com.elis.registrocalcio.handler;

import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.general.UserEvent;
import com.elis.registrocalcio.other.EmailServiceImpl;
import com.elis.registrocalcio.other.DateUtils;
import com.elis.registrocalcio.repository.general.EventRepository;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.repository.general.UserEventRepository;
import com.elis.registrocalcio.repository.general.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    @Autowired
    EventHandler eventHandler;

    private final Executor executor = Executors.newFixedThreadPool(5);

    public boolean areFieldsValid(EventDTO event){
        return validateEventCategory(event.getCategory()) && validateDate(DateUtils.StringToInstantConverter(event.getDate()));
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
//        Instant endOfToday = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS); // Can't create event in the givenDay, admin should do that almost 1 DAY before the event
        return !ObjectUtils.isEmpty(date) && Instant.now().isBefore(date);
    }

//    private boolean isAloneInDay(EventDTO event) {
//        boolean isEventAlone = true;
//        Instant date = DateUtils.StringToInstantConverter(event.getDate());
//        Instant startDay = date.truncatedTo(ChronoUnit.DAYS);
//        Instant nextDay = startDay.plus(1l, ChronoUnit.DAYS);
//        List<Event> eventsInDay = eventRepository.findEventInSameDateByDay(startDay, nextDay);
//        for (Event eventInDB : eventsInDay)
//            if(eventInDB.getCategory().equals(Category.getCategoryFromString(event.getCategory()))){
//                isEventAlone = false;
//                break;
//            }
//        return isEventAlone;
//    }

    public void delete(Event event){
        eventRepository.delete(event);
    }

    public List<Event> findAll(){
        return eventRepository.findAll();
    }

    public List<Event> findActiveEvents(String username){
        List<Long> subscribedEvents = userEventRepository.findEventsSubscribedByUser(username, Instant.now().plus(2, ChronoUnit.HOURS)).stream().map(Event::getId).collect(Collectors.toList());
        if(subscribedEvents.size() == 0) return eventRepository.findAllByPlayedIsFalseOrderByDateAsc(Instant.now().plus(2, ChronoUnit.HOURS));
        return eventRepository.findByIdNotIn(subscribedEvents, Instant.now().plus(2, ChronoUnit.HOURS));
    }
    public List<Event> findPastEvents(){
        return eventRepository.findAllByPlayedIsTrue(Instant.now());
    }

    public List<UserEvent> findEventPlayers(Long eventId, Integer page){
        int maxPlayers = eventHandler.findEventByIdCheckOptional(eventId).getCategory().numberOfAllowedPlayers();
        return userEventRepository.findPlayersOfEvent(eventId, PageRequest.of(page, maxPlayers));
    }

    public boolean isTeamsSizeValid(int team1, int team2){
        int major = Math.max(team1, team2);
        int minor = Math.min(team1, team2);
        if( major <= minor + 1) return true; //Teams can have at least one player of difference
        return false;
    }

    public Event save(Event event){
        return eventRepository.save(event);
    }

    /**
     * This method send an email to each user that want to now the creation of a new Event
     * @param event
     */
    public void newEventToNewsLetter(Event event) {
        List<List<String>> mailList = new ArrayList<>();
        boolean end = false;
        int page = 0;
        while(!end){
            List<String> current = userRepository.findNewsLetter(PageRequest.of(page, 25));
            if(current.size() > 0){
                page++;
                mailList.add(current);
            } else {
                end = true;
            }
        }
        if (mailList.size() > 0) {
            mailList.forEach(list -> executor.execute(() -> emailService.communicateNewEventToMailList(list, event.getCategory().toString(), event.getDate())));
        }
    }

    public void communicateRemoval(String appointed, String email, Event event) {
        executor.execute(() -> emailService.communicateRemoval(appointed, email, event));
    }


//    @Scheduled(fixedRate = 3600000) //Method called each hour
//    private void updateEventsStatus(){
//        eventRepository.updateEvents(Instant.now());
//        System.out.println("Events updated successfully");
//    }
}
