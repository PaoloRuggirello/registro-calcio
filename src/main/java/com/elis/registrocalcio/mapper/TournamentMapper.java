package com.elis.registrocalcio.mapper;

import com.elis.registrocalcio.dto.tournament.CreateTournamentRequestDTO;
import com.elis.registrocalcio.dto.tournament.CreateTournamentResponseDTO;
import com.elis.registrocalcio.enumPackage.Category;
import com.elis.registrocalcio.model.general.Tournament;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TournamentMapper {
    TournamentMapper INSTANCE = Mappers.getMapper(TournamentMapper.class);

    @Mapping(source = "category", target = "category", qualifiedByName = "convertCategory")
    Tournament convert(CreateTournamentRequestDTO createTournamentRequestDTO);
    CreateTournamentResponseDTO convert(Tournament tournament);

    @Named("convertCategory")
    static Category convertCategory(String category) {
        return Category.getCategoryFromString(category);
    }
}
