package com.elis.registrocalcio.dto;

import com.elis.registrocalcio.model.general.UserEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.util.ObjectUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserEventDTO {

    private Long eventId;
    private String playerUsername;
    private String team;

    public UserEventDTO() {
    }

    public UserEventDTO(UserEvent userEvent) {
        this.playerUsername = userEvent.getUser().getUsername();
        this.eventId = userEvent.getId();
        this.team = (!ObjectUtils.isEmpty(userEvent.getTeam())) ? userEvent.getTeam().toString() : null;
    }

    public UserEventDTO(String playerUsername, Long eventId){
        this.playerUsername = playerUsername;
        this.eventId = eventId;
    }

    public String getPlayerUsername() {
        return playerUsername;
    }

    public void setPlayerUsername(String playerUsername) {
        this.playerUsername = playerUsername;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }
}
