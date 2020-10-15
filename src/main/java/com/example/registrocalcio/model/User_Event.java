package com.example.registrocalcio.model;

import com.example.registrocalcio.enumPackage.Team;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.Instant;

@Entity
public class User_Event implements Serializable {

    @Id
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

    public User_Event() {
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
}
