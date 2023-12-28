package com.elis.registrocalcio.repository.general;

import com.elis.registrocalcio.model.general.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findAllByDateGreaterThanOrderByDate(Instant now);

    List<Tournament> findAllByDateLessThanOrderByDateDesc(Instant now);
}
