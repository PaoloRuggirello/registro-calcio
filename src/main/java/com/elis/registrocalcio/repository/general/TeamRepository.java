package com.elis.registrocalcio.repository.general;

import com.elis.registrocalcio.model.general.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
}
