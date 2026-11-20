package org.com.covoiturage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.com.covoiturage.DTOs.Reservation.ReservationRequest;
import org.com.covoiturage.DTOs.Reservation.ReservationResultResponse;
import org.com.covoiturage.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Réservations", description = "Réserver et annuler des places")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/passagers/{passagerId}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResultResponse reserve(@PathVariable Long passagerId, @Valid @RequestBody ReservationRequest request) {
        return reservationService.reserve(passagerId, request);
    }

    @PatchMapping("/passagers/{passagerId}/reservations/{id}/annuler")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long passagerId, @PathVariable Long id) {
        reservationService.cancel(passagerId, id);
    }
}