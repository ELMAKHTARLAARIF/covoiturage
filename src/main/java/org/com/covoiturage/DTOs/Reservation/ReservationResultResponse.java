package org.com.covoiturage.DTOs.Reservation;

import org.com.covoiturage.enums.StatutReservation;
import org.com.covoiturage.enums.StatutTrajet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResultResponse(
        Long id,
        Long trajetId,
        String villeMontee,
        String villeDescente,
        int nombrePlaces,
        BigDecimal montantTotal,
        LocalDateTime dateReservation,
        StatutReservation statut,
        int placesRestantes,
        StatutTrajet statutTrajet
) {}