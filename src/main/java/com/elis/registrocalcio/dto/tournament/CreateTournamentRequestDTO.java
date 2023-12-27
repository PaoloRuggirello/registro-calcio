package com.elis.registrocalcio.dto.tournament;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class CreateTournamentRequestDTO {
    @NotBlank
    public String name;
    @NotBlank
    public String category;
    @NotNull
    public Instant date;
}
