package com.example.registrocalcio.model;

import com.example.registrocalcio.dto.UserDTO;
import com.example.registrocalcio.enumPackage.Role;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.List;

@Entity
public class User implements Serializable {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "varchar(20) default 'USER'")
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;


    private Boolean isActive;

    @OneToMany(mappedBy = "creator")
    List<Event> events;

    @OneToMany(mappedBy = "user")
    List<UserEvent> playedMatches;



    public User() {

    }

    public User(UserDTO toRegister) {
        this.username = toRegister.getUsername();
        this.name = toRegister.getName();
        this.surname = toRegister.getSurname();
        this.email = toRegister.getEmail();
        this.password = toRegister.getPassword();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<UserEvent> getPlayedMatches() {
        return playedMatches;
    }

    public void setPlayedMatches(List<UserEvent> playedMatches) {
        this.playedMatches = playedMatches;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("username", username)
                .append("name", name)
                .append("surname", surname)
                .append("email", email)
                .append("password", password)
                .append("role", role)
                .toString();
    }
}
