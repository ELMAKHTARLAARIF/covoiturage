package org.com.covoiturage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.covoiturage.DTOs.Vehicule.VehiculeRequest;
import org.com.covoiturage.DTOs.Vehicule.VehiculeResponse;
import org.com.covoiturage.entity.Conducteur;
import org.com.covoiturage.entity.Vehicule;
import org.com.covoiturage.exception.BusinessRuleException;
import org.com.covoiturage.exception.ForbiddenException;
import org.com.covoiturage.exception.ResourceNotFoundException;
import org.com.covoiturage.mapper.VehiculeMapper;
import org.com.covoiturage.repository.ConducteurRepository;
import org.com.covoiturage.repository.TrajetRepository;
import org.com.covoiturage.repository.VehiculeRepository;
import org.com.covoiturage.service.VehiculeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final TrajetRepository trajetRepository;
    private final VehiculeMapper vehiculeMapper;

    @Override
    @Transactional
    public VehiculeResponse add(Long conducteurId, VehiculeRequest request) {
        Conducteur conducteur = findConducteur(conducteurId);
        String immatriculation = request.immatriculation().trim().toUpperCase();
        verifyImmatriculation(immatriculation, null);

        Vehicule vehicule = vehiculeMapper.toEntity(request);
        vehicule.setImmatriculation(immatriculation);
        vehicule.setConducteur(conducteur);

        Vehicule saved = vehiculeRepository.save(vehicule);
        return vehiculeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public VehiculeResponse update(Long conducteurId, Long vehiculeId, VehiculeRequest request) {
        Vehicule vehicule = findVehicule(vehiculeId);
        verifyOwner(vehicule, conducteurId);

        String immatriculation = request.immatriculation().trim().toUpperCase();
        verifyImmatriculation(immatriculation, vehiculeId);

        vehiculeMapper.update(request, vehicule);
        vehicule.setImmatriculation(immatriculation);

        Vehicule saved = vehiculeRepository.save(vehicule);
        log.info("Vehicle updated: id={}, driverId={}", vehiculeId, conducteurId);
        return vehiculeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculeResponse findById(Long vehiculeId) {
        return vehiculeMapper.toResponse(findVehicule(vehiculeId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> findByConducteur(Long conducteurId) {
        findConducteur(conducteurId);
        return vehiculeRepository.findByConducteurId(conducteurId).stream().map(vehiculeMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long conducteurId, Long vehiculeId) {
        Vehicule vehicule = findVehicule(vehiculeId);
        verifyOwner(vehicule, conducteurId);

        if (trajetRepository.existsByVehiculeId(vehiculeId)) {
            log.warn("Deletion denied: vehicle {} is associated with a trip", vehiculeId);
            throw new BusinessRuleException("Cannot delete vehicle " + vehiculeId + ": it is associated with a trip");
        }

        vehiculeRepository.delete(vehicule);
        log.info("Vehicle deleted: id={}, driverId={}", vehiculeId, conducteurId);
    }

    private Vehicule findVehicule(Long id) {
        return vehiculeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle Not Found!"));
    }

    private Conducteur findConducteur(Long id) {
        return conducteurRepository.findById(id).orElseThrow(() -> {
            log.warn("Driver Not Found: id={}", id);
            return new ResourceNotFoundException("Driver Not Found: id=" + id);
        });
    }

    private void verifyOwner(Vehicule vehicule, Long driverId) {
        if (!vehicule.getConducteur().getId().equals(driverId)) {
            log.warn("Access denied: driver {} is not the owner of vehicle {}", driverId, vehicule.getId());
            throw new ForbiddenException("Vehicle " + vehicule.getId() + " does not belong to driver " + driverId);
        }
    }

    private void verifyImmatriculation(String immatriculation, Long vehiculeId) {
        boolean taken = (vehiculeId == null) ? vehiculeRepository.existsByImmatriculationIgnoreCase(immatriculation) : vehiculeRepository.existsByImmatriculationIgnoreCaseAndIdNot(immatriculation, vehiculeId); //AndIdNot: Excludes the vehicle whose ID is provided (this Vehicle) .

        if (taken) {
            log.warn("License plate already exists");
            throw new BusinessRuleException("License plate already exists");
        }
    }
}