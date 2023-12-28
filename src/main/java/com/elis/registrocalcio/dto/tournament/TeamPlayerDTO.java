package com.elis.registrocalcio.dto.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamPlayerDTO {
    public String name;
    public String surname;
}
