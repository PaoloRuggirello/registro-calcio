package com.elis.registrocalcio.mapper;

import com.elis.registrocalcio.dto.tournament.CreateTournamentRequestDTO;
import com.elis.registrocalcio.dto.tournament.FullTournamentDTO;
import com.elis.registrocalcio.dto.tournament.TournamentDTO;
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
    @Mapping(source = "category", target = "category", qualifiedByName = "convertCategoryToString")
    TournamentDTO convert(Tournament tournament);
    @Mapping(source = "category", target = "category", qualifiedByName = "convertCategoryToString")
    FullTournamentDTO convertToFull(Tournament tournament);

    @Named("convertCategory")
    static Category convertCategory(String category) {
        return Category.getCategoryFromString(category);
    }

    @Named("convertCategoryToString")
    static String convertCategory(Category category) {
        return category.toString();
    }




}
