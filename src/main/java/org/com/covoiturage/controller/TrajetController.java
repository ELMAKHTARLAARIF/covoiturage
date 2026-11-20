package org.com.covoiturage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.com.covoiturage.DTOs.Trajet.TrajetRequest;
import org.com.covoiturage.DTOs.Trajet.TrajetReservationResponse;
import org.com.covoiturage.DTOs.Trajet.TrajetResponse;
import org.com.covoiturage.service.TrajetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Trajets", description = "Publication et cycle de vie des trajets")
public class TrajetController {

    private final TrajetService trajetService;

    @PostMapping("/conducteurs/{conducteurId}/trajets")
    @ResponseStatus(HttpStatus.CREATED)
    public TrajetResponse publish(@PathVariable Long conducteurId, @Valid @RequestBody TrajetRequest request) {
        return trajetService.publish(conducteurId, request);
    }

    @GetMapping("/trajets/{id}")
    public TrajetResponse getDetails(@PathVariable Long id) {
        return trajetService.getDetails(id);
    }

    @GetMapping("/conducteurs/{conducteurId}/trajets/{id}/reservations")
    public List<TrajetReservationResponse> getReservations(@PathVariable Long conducteurId, @PathVariable Long id) {
        return trajetService.getReservations(conducteurId, id);
    }

    @PatchMapping("/conducteurs/{conducteurId}/trajets/{id}/terminer")
    public TrajetResponse complete(@PathVariable Long conducteurId, @PathVariable Long id) {
        return trajetService.complete(conducteurId, id);
    }

    @PatchMapping("/conducteurs/{conducteurId}/trajets/{id}/annuler")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long conducteurId, @PathVariable Long id) {
        trajetService.cancel(conducteurId, id);
    }
}