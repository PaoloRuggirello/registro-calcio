package com.elis.registrocalcio.dto.tournament;

import com.elis.registrocalcio.enumPackage.Category;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTournamentResponseDTO {
    public Long id;
    public String name;
    public Category category;
}
