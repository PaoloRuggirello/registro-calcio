package com.example.registrocalcio.dto;

import com.example.registrocalcio.model.Event;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDTO {

    private Long id;
    private String category;
    private Date date;
    private UserDTO creator;

    public EventDTO() {
    }

    public EventDTO(Event event){
        this.id = event.getId();
        this.date = Date.from(event.getDate());
        this.category = event.getCategory().toString();
        this.creator = new UserDTO(event.getCreator());
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
