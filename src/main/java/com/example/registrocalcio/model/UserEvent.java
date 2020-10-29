package com.example.registrocalcio.model;

import com.example.registrocalcio.dto.UserEventDTO;
import com.example.registrocalcio.enumPackage.Team;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.util.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.Instant;

@Entity
public class UserEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Event event;

    @Column(columnDefinition = "boolean default false")
    private Boolean played = false;

    private Team team;

    @CreationTimestamp
    private Instant registrationTime;

    public UserEvent() {
    }
    public UserEvent(User user, Event event, UserEventDTO userEventDTO){
        this.user = user;
        this.event = event;
        this.played = !ObjectUtils.isEmpty(userEventDTO.getPlayed()) && userEventDTO.getPlayed();
        this.team = (!ObjectUtils.isEmpty(userEventDTO.getTeam())) ? Team.getTeamFromString(userEventDTO.getTeam()) : null;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Boolean getPlayed() {
        return played;
    }

    public void setPlayed(Boolean played) {
        this.played = played;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Instant getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Instant registrationTime) {
        this.registrationTime = registrationTime;
    }
}