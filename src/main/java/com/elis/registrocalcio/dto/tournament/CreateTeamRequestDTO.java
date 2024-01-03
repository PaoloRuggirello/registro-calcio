package com.elis.registrocalcio.dto.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTeamRequestDTO {
    public Long tournamentId;
    public String name;
    public String jersey;
}
