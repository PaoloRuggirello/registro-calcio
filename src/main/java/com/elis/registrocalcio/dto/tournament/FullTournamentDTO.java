package com.elis.registrocalcio.dto.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FullTournamentDTO {
    public Long id;
    public String name;
    public String category;
    public Instant date;
    public List<TeamDTO> teams;
}
