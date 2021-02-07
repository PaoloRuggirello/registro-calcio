package com.elis.registrocalcio.dto;

import com.elis.registrocalcio.enumPackage.Team;
import com.elis.registrocalcio.model.general.User;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerDTO {
    public String username;
    public String name;
    public String surname;
    public Team team;

    public PlayerDTO() {
    }

    public PlayerDTO(User player) {
        this.username = player.getUsername();
        this.name = player.getName();
        this.surname = player.getSurname();
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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
