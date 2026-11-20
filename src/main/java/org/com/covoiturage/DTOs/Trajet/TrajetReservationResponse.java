package org.com.covoiturage.DTOs.Trajet;

import org.com.covoiturage.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrajetReservationResponse(
        Long id,
        Long passagerId, String passagerNom, String passagerPrenom,
        String villeMontee, String villeDescente,
        int nombrePlaces, BigDecimal montantTotal,
        LocalDateTime dateReservation, StatutReservation statut
) {}