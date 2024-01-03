package com.elis.registrocalcio.mapper;

import com.elis.registrocalcio.dto.tournament.CreateTeamResponseDTO;
import com.elis.registrocalcio.model.general.Team;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TeamMapper {
    TeamMapper INSTANCE = Mappers.getMapper(TeamMapper.class);

    CreateTeamResponseDTO convert(Team team);
}
