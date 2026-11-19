package org.com.covoiturage.repository;

import org.com.covoiturage.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {
    boolean existsByVehiculeId(Long vehiculeId);


    @Query("""
           SELECT DISTINCT t FROM Trajet t
           JOIN FETCH t.vehicule v
           JOIN FETCH v.conducteur
           JOIN FETCH t.pointsArret p
           WHERE t.id = :id
           ORDER BY p.ordre ASC
           """)
    Optional<Trajet> findDetailById(@Param("id") Long id);
}