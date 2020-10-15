package com.example.registrocalcio.model;

import com.example.registrocalcio.dto.EventDTO;
import com.example.registrocalcio.enumPackage.Category;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.time.Instant;
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

    @OneToMany(mappedBy = "event")
    private List<User_Event> players;



    public Event() {
    }

    public Event(EventDTO eventDTO, User creator){
        this.category = Category.CALCIO_A_5.getCategoryFromString(eventDTO.getCategory());
        this.date = eventDTO.getDate().toInstant();
        this.creator = creator;
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

    public List<User_Event> getPlayers() {
        return players;
    }

    public void setPlayers(List<User_Event> players) {
        this.players = players;
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
