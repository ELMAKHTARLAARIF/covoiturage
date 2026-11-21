package org.com.covoiturage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_arret", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"trajet_id", "ordre"}),
        @UniqueConstraint(columnNames = {"trajet_id", "ville"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PointArret {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trajet_id", nullable = false)
    private Trajet trajet;

    @Column(nullable = false) private String ville;

    @Column(nullable = false) private int ordre;

    @Column(name = "heure_passage", nullable = false)
    private LocalDateTime heurePassage;

    @Column(name = "prix_depuis_depart", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixDepuisDepart;

    public BigDecimal prixVers(PointArret descente) {
        return descente.getPrixDepuisDepart().subtract(this.getPrixDepuisDepart()); // this it mean pointMontee that call this method so like we passe pointMontee and do that pointDescent.prix - pointMontee.prix
    }
}