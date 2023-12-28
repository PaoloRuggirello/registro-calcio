package com.elis.registrocalcio.controller;

import com.elis.registrocalcio.dto.Token;
import com.elis.registrocalcio.dto.tournament.CreateTournamentRequestDTO;
import com.elis.registrocalcio.dto.tournament.CreateTournamentResponseDTO;
import com.elis.registrocalcio.dto.tournament.FindTournamentsDTO;
import com.elis.registrocalcio.dto.tournament.FullTournamentDTO;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
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
        List<Tournament> tournaments = tournamentRepository.findAllByDateGreaterThanOrderByDate(today); // TODO add pagination
        return new FindTournamentsDTO(TournamentMapper.INSTANCE.convert(tournaments));
    }

    @GetMapping(path = "/past", produces = APPLICATION_JSON_VALUE)
    public FindTournamentsDTO findPastTournaments(@RequestHeader("Authorization") Token userToken) {
        log.info("Obtaining list of past tournaments");
        tokenHandler.checkToken(userToken);
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        List<Tournament> tournaments = tournamentRepository.findAllByDateLessThanOrderByDateDesc(today); // TODO: add pagination
        return new FindTournamentsDTO(TournamentMapper.INSTANCE.convert(tournaments));
    }

    @GetMapping(path = "/{tournamentId}", produces = APPLICATION_JSON_VALUE)
    public FullTournamentDTO findById(@RequestHeader("Authorization") Token userToken, @PathVariable("tournamentId") Long tournamentId) {
        tokenHandler.checkToken(userToken);
        log.info("Finding tournament with id: {}", tournamentId);
        Tournament tournament = tournamentRepository.findWithTeamsById(tournamentId).orElseThrow(() -> new IllegalArgumentException(format("Tournament by id %s not found", tournamentId)));
        log.debug("Found tournament: {}", tournament);
        return TournamentMapper.INSTANCE.convertToFull(tournament);
    }

}
