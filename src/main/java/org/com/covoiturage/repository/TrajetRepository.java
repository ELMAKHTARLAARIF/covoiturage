package org.com.covoiturage.repository;

import org.com.covoiturage.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {
    boolean existsByVehiculeId(Long vehiculeId);
}