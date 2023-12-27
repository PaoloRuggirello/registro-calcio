package com.elis.registrocalcio.dto.tournament;

import com.elis.registrocalcio.enumPackage.Category;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentDTO {
    public Long id;
    public String name;
    public Instant date;
    public Category category;
}
