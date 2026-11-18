package org.com.covoiturage.service;

import org.com.covoiturage.DTOs.VehiculeRequest;
import org.com.covoiturage.DTOs.VehiculeResponse;

import java.util.List;

public interface VehiculeService {
    VehiculeResponse add(Long conducteurId, VehiculeRequest request);
    VehiculeResponse update(Long conducteurId, Long vehiculeId, VehiculeRequest request);
    VehiculeResponse findById(Long vehiculeId);
    List<VehiculeResponse> findByConducteur(Long conducteurId);
    void delete(Long conducteurId, Long vehiculeId);
}