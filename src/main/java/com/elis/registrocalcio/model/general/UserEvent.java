package com.elis.registrocalcio.model.general;

import com.elis.registrocalcio.dto.UserEventDTO;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.enumPackage.Team;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.util.ObjectUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import static java.util.Arrays.asList;

@Entity
public class UserEvent implements Serializable, Comparable<UserEvent> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Event event;

    private Team team;

    @CreationTimestamp
    private Instant registrationTime;

    public UserEvent() {
    }
    public UserEvent(User user, Event event){
        this.user = user;
        this.event = event;
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

    @Override
    public int compareTo(UserEvent userEvent) {
        List<Role> premiumRoles = asList(Role.USER, Role.ADMIN);
        int thisEntityScore = !premiumRoles.contains(this.user.getRole()) ? 1 : 0;
        int userEventScore = !premiumRoles.contains(userEvent.user.getRole()) ? 1 : 0;
        return thisEntityScore - userEventScore;
    }
}
