package org.com.covoiturage.repository;

import org.com.covoiturage.entity.Reservation;
import org.com.covoiturage.enums.StatutReservation;
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





    // used by trajet service

    // seats already booked on a trip
    @Query("""
       SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r
       WHERE r.pointMontee.trajet.id = :trajetId AND r.statut = :statut
       """)
    Long sumPlacesByTrajetAndStatut(@Param("trajetId") Long trajetId,
                                    @Param("statut") StatutReservation statut);

    // driver view: all reservations of a trip, with passenger and stops in one query
    @Query("""
       SELECT r FROM Reservation r
       JOIN FETCH r.passager
       JOIN FETCH r.pointMontee pm
       JOIN FETCH r.pointDescente
       WHERE pm.trajet.id = :trajetId
       ORDER BY r.dateReservation ASC
       """)
    List<Reservation> findByTrajetIdWithDetails(@Param("trajetId") Long trajetId);

    // used when a trip is cancelled
    @Query("""
       SELECT r FROM Reservation r
       WHERE r.pointMontee.trajet.id = :trajetId AND r.statut = :statut
       """)
    List<Reservation> findByTrajetIdAndStatut(@Param("trajetId") Long trajetId,
                                              @Param("statut") StatutReservation statut);


    @Query("""
       SELECT COUNT(r) > 0 FROM Reservation r
       WHERE r.passager.id = :passagerId
         AND r.pointMontee.trajet.id = :trajetId
         AND r.statut = :statut
       """)
    boolean hasReservation(@Param("passagerId") Long passagerId,
                           @Param("trajetId") Long trajetId,
                           @Param("statut") StatutReservation statut);
}
