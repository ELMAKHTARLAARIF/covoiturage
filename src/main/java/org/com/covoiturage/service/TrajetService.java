package org.com.covoiturage.service;

import org.com.covoiturage.DTOs.Trajet.TrajetRequest;
import org.com.covoiturage.DTOs.Trajet.TrajetReservationResponse;
import org.com.covoiturage.DTOs.Trajet.TrajetResponse;

import java.util.List;

public interface TrajetService {
    TrajetResponse publish(Long conducteurId, TrajetRequest request);
    TrajetResponse getDetails(Long trajetId);
    List<TrajetReservationResponse> getReservations(Long conducteurId, Long trajetId);
    TrajetResponse complete(Long conducteurId, Long trajetId);
    void cancel(Long conducteurId, Long trajetId);
}