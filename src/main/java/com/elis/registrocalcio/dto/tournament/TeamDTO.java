package com.elis.registrocalcio.dto.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDTO {
    public String name;
    public String jersey;
    public List<TeamPlayerDTO> players;
}
