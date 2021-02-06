package com.elis.registrocalcio.model.general;

import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.dto.EventDTO;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.util.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Entity
public class Event implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private Instant date;

    @ManyToOne
    @JoinColumn(name = "creator", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "event", fetch = FetchType.EAGER)
    private List<UserEvent> players;

    @Column(columnDefinition = "boolean default false")
    private Boolean played = false;


    public Event() {
    }

    public Event(EventDTO eventDTO, User creator){
        this.category = Category.getCategoryFromString(eventDTO.getCategory());
        this.date = eventDTO.getDate().toInstant();
        this.creator = creator;
        this.played = !ObjectUtils.isEmpty(eventDTO.played) && eventDTO.getPlayed();
    }
    public Event(Category category, Instant date, User creator){
        this.category = category;
        this.date = date;
        this.creator = creator;
    }

    public Event(Category category, Instant date, User creator, Boolean played) {
        this.category = category;
        this.date = date;
        this.creator = creator;
        this.played = played;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<UserEvent> getPlayers() {
        return players;
    }

    public void setPlayers(List<UserEvent> players) {
        this.players = players;
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
                .append("players", players)
                .toString();
    }
}
