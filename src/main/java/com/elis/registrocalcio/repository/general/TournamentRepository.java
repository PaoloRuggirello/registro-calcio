package com.elis.registrocalcio.repository.general;

import com.elis.registrocalcio.model.general.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
}
