package org.com.covoiturage.DTOs.Reservation;

import org.com.covoiturage.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(Long id, Long trajetId, String villeMontee, String villeDescente,
                                  int nombrePlaces, BigDecimal montantTotal,
                                  LocalDateTime dateReservation, StatutReservation statut) {}