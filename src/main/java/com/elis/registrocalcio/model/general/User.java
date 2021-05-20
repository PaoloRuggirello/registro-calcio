package com.elis.registrocalcio.model.general;

import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.dto.UserDTO;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.util.ObjectUtils;

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

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(name = "news_letter", columnDefinition = "boolean default false")
    private Boolean newsLetter = false;

    @OneToMany(mappedBy = "creator")
    List<Event> events;

    @OneToMany(mappedBy = "user")
    List<UserEvent> playedMatches;

    public User() {

    }

    public User(String username, String name, String surname, String email, String password) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.isActive = true;
    }

    public User(String username, String name, String surname, String email, String password, Boolean newsLetter) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.isActive = true;
        this.newsLetter = newsLetter;
    }

    public User(UserDTO toRegister) {
        this.username = toRegister.getUsername();
        this.name = toRegister.getName();
        this.surname = toRegister.getSurname();
        this.email = toRegister.getEmail();
        this.password = toRegister.getPassword();
        this.isActive = ObjectUtils.isEmpty(toRegister.isActive) || toRegister.isActive;
        this.newsLetter = !ObjectUtils.isEmpty(toRegister.newsletter) && toRegister.newsletter; //If parameter not passed set newsLetter to false, otherwise set newsLetter as passed
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

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getNewsLetter() {
        return newsLetter;
    }

    public void setNewsLetter(Boolean newsLetter) {
        this.newsLetter = newsLetter;
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
