package org.com.covoiturage.entity;

import jakarta.persistence.*;
import lombok.*;
import org.com.covoiturage.enums.StatutTrajet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trajets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trajet {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @Column(name = "ville_depart", nullable = false) private String villeDepart;
    @Column(name = "ville_arrivee", nullable = false) private String villeArrivee;
    @Column(name = "date_depart", nullable = false) private LocalDateTime dateDepart;
    @Column(name = "places_proposees", nullable = false) private int placesProposees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTrajet statut;

    @OneToMany(mappedBy = "trajet", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    @Builder.Default
    private List<PointArret> pointsArret = new ArrayList<>();

    public Conducteur getConducteur() {
        return vehicule.getConducteur();
    }


    public void addPointArret(PointArret p) {
        p.setTrajet(this);
        pointsArret.add(p);
    }
}