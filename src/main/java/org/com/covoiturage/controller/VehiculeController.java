package org.com.covoiturage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.com.covoiturage.DTOs.VehiculeRequest;
import org.com.covoiturage.DTOs.VehiculeResponse;
import org.com.covoiturage.service.VehiculeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Véhicules", description = "Gestion des véhicules des conducteurs")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @PostMapping("/conducteurs/{conducteurId}/vehicules")
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculeResponse add(@PathVariable Long conducteurId, @Valid @RequestBody VehiculeRequest request) {
        return vehiculeService.add(conducteurId, request);
    }

    @GetMapping("/conducteurs/{conducteurId}/vehicules")
    public List<VehiculeResponse> findByConducteur(@PathVariable Long conducteurId) {
        return vehiculeService.findByConducteur(conducteurId);
    }

    @GetMapping("/vehicules/{id}")
    public VehiculeResponse findById(@PathVariable Long id) {
        return vehiculeService.findById(id);
    }

    @PutMapping("/conducteurs/{conducteurId}/vehicules/{id}")
    public VehiculeResponse update(@PathVariable Long conducteurId, @PathVariable Long id, @Valid @RequestBody VehiculeRequest request) {
        return vehiculeService.update(conducteurId, id, request);
    }

    @DeleteMapping("/conducteurs/{conducteurId}/vehicules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long conducteurId, @PathVariable Long id) {
        vehiculeService.delete(conducteurId, id);
    }
}