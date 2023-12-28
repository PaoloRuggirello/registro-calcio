package com.elis.registrocalcio.controller;

import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.dto.tournament.CreateTournamentRequestDTO;
import com.elis.registrocalcio.dto.tournament.CreateTournamentResponseDTO;
import com.elis.registrocalcio.dto.tournament.FindTournamentsDTO;
import com.elis.registrocalcio.enumPackage.Role;
import com.elis.registrocalcio.handler.TokenHandler;
import com.elis.registrocalcio.mapper.TournamentMapper;
import com.elis.registrocalcio.model.general.Tournament;
import com.elis.registrocalcio.model.security.SecurityToken;
import com.elis.registrocalcio.repository.general.TournamentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/tournaments")
@Slf4j
public class TournamentController {

    @Autowired
    private TokenHandler tokenHandler;
    @Autowired
    private TournamentRepository tournamentRepository;

    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public CreateTournamentResponseDTO createTournament(@RequestBody CreateTournamentRequestDTO createTournamentRequestDTO, @RequestHeader("Authorization") Token userToken) {
        SecurityToken token = tokenHandler.checkToken(userToken, Role.ADMIN);
        log.info("{} is creating a Tournament. Event info: {}", token.getUsername(), createTournamentRequestDTO);
        Tournament tournament = TournamentMapper.INSTANCE.convert(createTournamentRequestDTO);
        tournament = tournamentRepository.save(tournament);
        return TournamentMapper.INSTANCE.convert(tournament);
    }

    @GetMapping(path = "/active", produces = APPLICATION_JSON_VALUE)
    public FindTournamentsDTO findActiveTournaments(@RequestHeader("Authorization") Token userToken) {
        log.info("Obtaining list of active tournaments");
        tokenHandler.checkToken(userToken);
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        List<Tournament> tournaments = tournamentRepository.findAllByDateGreaterThanOrderByDate(today);
        return new FindTournamentsDTO(TournamentMapper.INSTANCE.convert(tournaments));
    }

}
