package org.com.covoiturage.service;

import org.com.covoiturage.DTOs.Reservation.ReservationRequest;
import org.com.covoiturage.DTOs.Reservation.ReservationResultResponse;

public interface ReservationService {
    ReservationResultResponse reserve(Long passagerId, ReservationRequest request);
    void cancel(Long passagerId, Long reservationId);
}