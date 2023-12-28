package com.elis.registrocalcio.model.general;

import com.elis.registrocalcio.enumPackage.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.repository.EntityGraph;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;

@NamedEntityGraph(
        name = "withTeams",
        attributeNodes = @NamedAttributeNode("teams")
)
@Entity(name = "Tournament")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private Instant date;

    @CreationTimestamp
    private Instant creationTime;

    @OneToMany(mappedBy = "tournament", fetch = FetchType.LAZY)
    private List<Team> teams;
}
