package com.elis.registrocalcio.dto;

import com.elis.registrocalcio.model.general.User;
import com.elis.registrocalcio.model.general.UserEvent;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerDTO {
    public String username;
    public String name;
    public String surname;
    public String team;
    public String role;

    public PlayerDTO() {
    }

    public PlayerDTO(UserEvent player) {
        this.username = player.getUser().getUsername();
        this.name = player.getUser().getName();
        this.surname = player.getUser().getSurname();
        this.role = player.getUser().getRole().name();
        this.team = (player.getTeam() != null) ? player.getTeam().toString() : null;
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

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }
}
