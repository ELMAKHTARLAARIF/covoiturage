package org.com.covoiturage.repository;

import org.com.covoiturage.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
           SELECT r FROM Reservation r
           JOIN FETCH r.pointMontee pm
           JOIN FETCH pm.trajet
           JOIN FETCH r.pointDescente
           WHERE r.passager.id = :passagerId
           ORDER BY r.dateReservation DESC
           """)
    List<Reservation> findHistoricByPassagerId(@Param("passagerId") Long passagerId);
}
