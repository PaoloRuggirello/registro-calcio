package com.elis.registrocalcio;

import com.elis.registrocalcio.controller.EventController;
import com.elis.registrocalcio.dto.CategoriesDTO;
import com.elis.registrocalcio.dto.EventDTO;
import com.elis.registrocalcio.dto.PlayerDTO;
import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.dto.UserDTO;
import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.enumPackage.FootballRegisterException;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.enumPackage.Team;
import com.elis.registrocalcio.handler.TokenHandler;
import com.elis.registrocalcio.handler.UserHandler;
import com.elis.registrocalcio.model.general.Event;
import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.general.UserEvent;
import com.elis.registrocalcio.other.DateUtils;
import com.elis.registrocalcio.repository.general.EventRepository;
import com.elis.registrocalcio.repository.general.UserEventRepository;
import com.elis.registrocalcio.repository.general.UserRepository;
import com.elis.registrocalcio.repository.security.SecurityTokenRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
public class EventControllerTest {
    @Autowired
    EventController eventController;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserHandler userHandler;
    @Autowired
    TokenHandler tokenHandler;
    @Autowired
    SecurityTokenRepository securityTokenRepository;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    UserEventRepository userEventRepository;

    @BeforeEach
    public void dropDBs(){
        securityTokenRepository.deleteAll();
        userEventRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testInstantFunctions(){
        Instant date = Instant.now();
        Instant startDay = date.truncatedTo(ChronoUnit.DAYS);
        Instant nextDay = startDay.plus(1l, ChronoUnit.DAYS);
        Instant now = Instant.now();
        System.out.println(now);
        DateUtils.getHourFromInstant(now);
    }

    @Test
    public void createEventTest() throws InvalidKeySpecException, NoSuchAlgorithmException, SQLIntegrityConstraintViolationException, ParseException {
        User user = new User("user.user", "name", "surname", "user@email.it", userHandler.passwordEncryption("password"));
        User admin = new User("admin.admin", "name", "surname", "admin@email.it", userHandler.passwordEncryption("password"));
        admin.setRole(Role.ADMIN);
        user = userRepository.save(user);
        userRepository.save(admin);
        Token userToken = tokenHandler.createToken(user.getUsername());
        Token adminToken = tokenHandler.createToken(admin.getUsername());
        Token tokenToUse = new Token();

        EventDTO eventDTO = new EventDTO(Category.CALCIO_A_5.toString(), convertDate(new Date()), new UserDTO(user));

        //Users can't create events
        tokenToUse.token = userToken.token;
        Throwable testException = assertThrows(ResponseStatusException.class, () -> eventController.createEvent(eventDTO, tokenToUse));
        assertThat(testException.getMessage(), containsString(FootballRegisterException.PERMISSION_DENIED.toString()));

        //Create event invalid date
        tokenToUse.token = adminToken.token;
        eventDTO.creator = new UserDTO(admin);
        testException = assertThrows(ResponseStatusException.class, () -> eventController.createEvent(eventDTO, tokenToUse));
        assertThat(testException.getMessage(), containsString(FootballRegisterException.INVALID_REGISTRATION_FIELDS.toString()));

        //Create event correct date
        // -- adding hours to date
        String threeHoursFromNow = convertDate(Date.from(Instant.now().plus(3, ChronoUnit.HOURS)));
        tokenToUse.token = adminToken.token;
        eventDTO.date = threeHoursFromNow;
        assertNotNull(eventController.createEvent(eventDTO, tokenToUse));
//        testException = assertThrows(ResponseStatusException.class, () -> eventController.createEvent(eventDTO, tokenToUse));
//        assertThat(testException.getMessage(), containsString(FootballRegisterException.INVALID_REGISTRATION_FIELDS.toString()));

        //Create correctly an event
        String tomorrow = convertDate(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        tokenToUse.token = adminToken.token;
        eventDTO.date = tomorrow;
        EventDTO createEventDTO = eventController.createEvent(eventDTO, tokenToUse);
        assertTrue(eventRepository.findById(createEventDTO.id).isPresent());
        assertThat(eventRepository.findById(createEventDTO.id).get().getCategory().toString(), equalTo(eventDTO.getCategory()));

        //Try to create another event of the same category in the same day
        tokenToUse.token = adminToken.token;
        assertNotNull(eventController.createEvent(eventDTO, tokenToUse));
//        testException = assertThrows(ResponseStatusException.class, () -> eventController.createEvent(eventDTO, tokenToUse));
//        assertThat(testException.getMessage(), containsString(FootballRegisterException.EVENT_ALREADY_EXIST_IN_THE_GIVEN_DAY.toString()));

        //Try to create event of another category in the same day, should works
        eventDTO.category = Category.CALCIO_A_7.toString();
        tokenToUse.token = adminToken.token;
        EventDTO calcioA7 = eventController.createEvent(eventDTO, tokenToUse);
        assertTrue(eventRepository.findById(calcioA7.id).isPresent());
        assertThat(eventRepository.findById(calcioA7.id).get().getCategory().toString(), equalTo(eventDTO.getCategory()));
        assertThat(eventRepository.findAll(), Matchers.hasSize(4));
    }

    @Test
    public void testDeleteEvent() throws InvalidKeySpecException, NoSuchAlgorithmException {
        User admin = new User("admin.admin", "name", "surname", "admin@email.it", userHandler.passwordEncryption("password"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        Token adminToken = tokenHandler.createToken(admin.getUsername());
        Event eventToDelete = new Event(Category.CALCIO_A_5, Instant.now().plus(2, ChronoUnit.DAYS), admin);
        eventToDelete = eventRepository.save(eventToDelete);
        assertThat(eventRepository.findAll(), Matchers.hasSize(1));

        //Call endpoint to delete
        EventDTO deleted = eventController.deleteEvent(eventToDelete.getId(), adminToken);
        assertThat(deleted.category, equalTo(eventToDelete.getCategory().toString()));
        assertThat(eventRepository.findAll(), hasSize(0));
    }

    @Test
    public void testFindAll() throws InvalidKeySpecException, NoSuchAlgorithmException {
        User user = new User("user.user", "name", "surname", "user@email.it", userHandler.passwordEncryption("password"));
        User admin = new User("admin.admin", "name", "surname", "admin@email.it", userHandler.passwordEncryption("password"));
        userRepository.save(user);
        userRepository.save(admin);
        Token userToken = tokenHandler.createToken(user.getUsername());
        Token token = new Token();

        //Creating some events
        List<Event> events = new ArrayList<>();
        events.add(new Event(Category.CALCIO_A_5, Instant.now().plus(2, ChronoUnit.DAYS), admin));
        events.add(new Event(Category.CALCIO_A_7, Instant.now().plus(3, ChronoUnit.DAYS), admin));
        events.add(new Event(Category.CALCIO_A_11, Instant.now().plus(4, ChronoUnit.DAYS), admin));
        events.add(new Event(Category.CALCIO_A_5, Instant.now().plus(-2, ChronoUnit.DAYS), admin));
        events.add(new Event(Category.CALCIO_A_7, Instant.now().plus(-3, ChronoUnit.DAYS), admin));
        events.add(new Event(Category.CALCIO_A_11, Instant.now().plus(4, ChronoUnit.DAYS), admin));
        eventRepository.saveAll(events);

        //Get events from service
        token.token = userToken.token;
        List<EventDTO> eventsFromDB = eventController.findAll(token);
        assertThat(eventsFromDB, hasSize(6));
        assertThat(eventsFromDB.stream().filter(eventDTO -> eventDTO.getCategory().equals(Category.CALCIO_A_5.toString())).collect(Collectors.toList()), hasSize(2));
        assertThat(eventsFromDB.stream().filter(eventDTO -> eventDTO.getCategory().equals(Category.CALCIO_A_7.toString())).collect(Collectors.toList()), hasSize(2));
        assertThat(eventsFromDB.stream().filter(eventDTO -> eventDTO.getCategory().equals(Category.CALCIO_A_11.toString())).collect(Collectors.toList()), hasSize(2));

        //GetActiveEvents
        token.token = userToken.token;
        eventsFromDB = eventController.findActiveEvents(token);
        assertThat(eventsFromDB, hasSize(4));
        assertThat(eventsFromDB.stream().filter(eventDTO -> eventDTO.getCategory().equals(Category.CALCIO_A_5.toString())).collect(Collectors.toList()), hasSize(1));
        assertThat(eventsFromDB.stream().filter(eventDTO -> eventDTO.getCategory().equals(Category.CALCIO_A_7.toString())).collect(Collectors.toList()), hasSize(1));
        assertThat(eventsFromDB.stream().filter(eventDTO -> eventDTO.getCategory().equals(Category.CALCIO_A_11.toString())).collect(Collectors.toList()), hasSize(2));

        //GetPastEvents
        token.token = userToken.token;
        eventsFromDB = eventController.findPastEvents(token);
        assertThat(eventsFromDB, hasSize(2));
        assertThat(eventsFromDB.stream().filter(eventDTO -> eventDTO.getCategory().equals(Category.CALCIO_A_5.toString())).collect(Collectors.toList()), hasSize(1));
        assertThat(eventsFromDB.stream().filter(eventDTO -> eventDTO.getCategory().equals(Category.CALCIO_A_7.toString())).collect(Collectors.toList()), hasSize(1));
    }

    @Test
    public void testFinEventAndFindPlayers() throws InvalidKeySpecException, NoSuchAlgorithmException {
        User user = new User("user.user", "name", "surname", "user@email.it", userHandler.passwordEncryption("password"));
        User admin = new User("admin.admin", "name", "surname", "admin@email.it", userHandler.passwordEncryption("password"));
        userRepository.save(user);
        userRepository.save(admin);
        Token userToken = tokenHandler.createToken(user.getUsername());
        Token token = new Token();

        Event event = eventRepository.save(new Event(Category.CALCIO_A_5, Instant.now().plus(2, ChronoUnit.DAYS), admin));
        List<User> players = new ArrayList<>();
        players.add(new User("user.user1", "name1", "surname", "1user@email.it", "password"));
        players.add(new User("user.user2", "name2", "surname", "2user@email.it", "password"));
        players.add(new User("user.user3", "name3", "surname", "3user@email.it", "password"));
        players.add(new User("user.user4", "name4", "surname", "4user@email.it", "password"));
        players.add(new User("user.user5", "name5", "surname", "5user@email.it", "password"));
        players.add(new User("user.user6", "name6", "surname", "6user@email.it", "password"));
        userRepository.saveAll(players);
        List<UserEvent> userEventList = players.stream().map(player -> new UserEvent(player, event)).collect(Collectors.toList());
        userEventRepository.saveAll(userEventList);

        //Check findEvent combined with findPlayers
        token.token = userToken.token;
        EventDTO eventFromService = eventController.findEvent(event.getId(), token);
        token.token = userToken.token;
        List<PlayerDTO> playersFromService = eventController.findPlayers(event.getId(), 0, token);

        //Assertions
        assertThat(eventFromService.getCategory(), equalTo(Category.CALCIO_A_5.toString()));
        assertThat(eventFromService.getCreator().getUsername(), equalTo(null));
        assertThat(eventFromService.getFreeSeats(), equalTo(Category.CALCIO_A_5.numberOfAllowedPlayers() - playersFromService.size()));
        players.forEach( player -> assertTrue(playersFromService.stream().map(PlayerDTO::getName).collect(Collectors.toList()).contains(player.getName())));
    }


    @Test
    public void testSetTeam(){
        User admin = new User("admin.admin", "name", "surname", "admin@email.it", "password");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        Token adminToken = tokenHandler.createToken(admin.getUsername());
        Token token = new Token();

        Event event = eventRepository.save(new Event(Category.CALCIO_A_5, Instant.now().plus(2, ChronoUnit.DAYS), admin));
        event = eventRepository.save(event);
        List<User> players = new ArrayList<>();
        players.add(new User("user.user1", "name", "surname", "eamil1@mail.it", "password"));
        players.add(new User("user.user2", "name", "surname", "amail2@mail.it", "password"));
        userRepository.saveAll(players);
        List<UserEvent> userEventList = new ArrayList<>();
        userEventList.add(new UserEvent(players.get(0),event));
        userEventList.add(new UserEvent(players.get(1),event));
        userEventRepository.saveAll(userEventList);


        token.token = adminToken.token;
        eventController.setTeam(event.getId(), Collections.singletonList(players.get(0).getUsername()), Collections.singletonList(players.get(1).getUsername()), token);
        List<UserEvent> eventsFromDB = userEventRepository.findAll();
        assertThat(eventsFromDB.size(), equalTo(2));
        assertNotNull(eventsFromDB.get(0).getTeam());
        assertThat(eventsFromDB.get(0).getTeam(), equalTo(Team.BLACK));
        assertNotNull(eventsFromDB.get(1).getTeam());
        assertThat(eventsFromDB.get(1).getTeam(), equalTo(Team.WHITE));
    }

    @Test
    public void testModifyEvent(){
        User admin = new User("admin.admin", "name", "surname", "admin@email.it", "password", true);
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        EventDTO event = new EventDTO(Category.CALCIO_A_11.toString(), convertDate(Date.from(Instant.now().plus(3, ChronoUnit.DAYS))), new UserDTO(admin));
        eventController.createEvent(event, tokenHandler.createToken(admin.getUsername()));

        List<Event> allEvents = eventRepository.findAll();
        assertThat(allEvents, hasSize(1));
        assertThat(allEvents.get(0).getCategory(), equalTo(Category.CALCIO_A_11));
        assertTrue(allEvents.get(0).getDate().isAfter(Instant.now()));

        event.setCategory(Category.CALCIO_A_5.toString());
        event.setDate(convertDate(Date.from(Instant.now().minus(3, ChronoUnit.DAYS))));
        event.setId(allEvents.get(0).getId());

        eventController.modifyEvent(event, tokenHandler.createToken(admin.getUsername()));
        allEvents = eventRepository.findAll();
        assertThat(allEvents, hasSize(1));
        assertThat(allEvents.get(0).getCategory(), equalTo(Category.CALCIO_A_5));
        assertTrue(allEvents.get(0).getDate().isBefore(Instant.now()));

    }

    @Test
    public void testGetCategories() throws InvalidKeySpecException, NoSuchAlgorithmException {
        User admin = new User("admin.admin", "name", "surname", "admin@email.it", userHandler.passwordEncryption("password"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        Token token = tokenHandler.createToken(admin.getUsername());
        CategoriesDTO categoriesDTO = eventController.getCategories(token);
        assertNotNull(categoriesDTO);
    }

    private String convertDate(Date current){
        return DateUtils.getCompleteDateFormatter().format(current);
    }

}
