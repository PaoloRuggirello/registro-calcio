package com.elis.registrocalcio.dto;

import com.elis.registrocalcio.model.Event;
import com.elis.registrocalcio.model.UserEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDTO {

    public Long id;
    public String category;
    public Date date;
    public UserDTO creator;
    public Boolean played;

    public EventDTO() {
    }

    public EventDTO(Event event){
        this.id = event.getId();
        this.date = Date.from(event.getDate());
        this.category = event.getCategory().toString();
        this.creator = new UserDTO(event.getCreator());
        this.played = event.getPlayed();
    }
    public EventDTO(UserEvent userEvent){
        this.id = userEvent.getId();
        this.date = Date.from(userEvent.getEvent().getDate());
        this.category = userEvent.getEvent().getCategory().toString();
        this.creator = new UserDTO(userEvent.getEvent().getCreator());
        this.played = userEvent.getEvent().getPlayed();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public UserDTO getCreator() {
        return creator;
    }

    public void setCreator(UserDTO creator) {
        this.creator = creator;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getPlayed() {
        return played;
    }

    public void setPlayed(Boolean played) {
        this.played = played;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("category", category)
                .append("date", date)
                .append("creator", creator)
                .toString();
    }
}
