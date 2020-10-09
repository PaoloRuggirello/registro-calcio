package com.example.registrocalcio.controller;

import com.example.registrocalcio.dto.EventDTO;
import com.example.registrocalcio.handler.UserHandler;
import com.example.registrocalcio.model.Event;
import com.example.registrocalcio.model.User;
import com.example.registrocalcio.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    UserHandler userHandler;

    @Autowired
    EventRepository eventRepository;

    @PostMapping("/create")
    public void createEvent(@RequestBody EventDTO eventToCreate){
        System.out.println(eventToCreate);
        User creator = userHandler.findUserByUsername(eventToCreate.getCreator().getUsername()).get();
        Event event = new Event(eventToCreate, creator);
        System.out.println("New event : " + event);
        eventRepository.save(event);

    }


}
