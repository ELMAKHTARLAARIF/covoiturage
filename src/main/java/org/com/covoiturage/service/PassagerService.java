package org.com.covoiturage.service;

import org.com.covoiturage.DTOs.Passager.PassagerRequest;
import org.com.covoiturage.DTOs.Passager.PassagerResponse;
import org.com.covoiturage.DTOs.Reservation.ReservationResponse;

import java.util.List;

public interface PassagerService {
    PassagerResponse create(PassagerRequest request);
    List<PassagerResponse> list();
    PassagerResponse findById(Long id);
    PassagerResponse update(Long id, PassagerRequest request);
    PassagerResponse searchByEmail(String email);
    List<ReservationResponse> historicReservations(Long passagerId);
}