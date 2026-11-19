package org.com.covoiturage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.covoiturage.DTOs.PointArretRequest;
import org.com.covoiturage.DTOs.TrajetRequest;
import org.com.covoiturage.DTOs.TrajetReservationResponse;
import org.com.covoiturage.DTOs.TrajetResponse;
import org.com.covoiturage.entity.PointArret;
import org.com.covoiturage.entity.Reservation;
import org.com.covoiturage.entity.Trajet;
import org.com.covoiturage.entity.Vehicule;
import org.com.covoiturage.enums.StatutReservation;
import org.com.covoiturage.enums.StatutTrajet;
import org.com.covoiturage.exception.BusinessRuleException;
import org.com.covoiturage.exception.ForbiddenException;
import org.com.covoiturage.exception.InvalidRequestException;
import org.com.covoiturage.exception.ResourceNotFoundException;
import org.com.covoiturage.mapper.TrajetMapper;
import org.com.covoiturage.repository.ConducteurRepository;
import org.com.covoiturage.repository.ReservationRepository;
import org.com.covoiturage.repository.TrajetRepository;
import org.com.covoiturage.repository.VehiculeRepository;
import org.com.covoiturage.service.TrajetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrajetServiceImpl implements TrajetService {

    private final TrajetRepository trajetRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final ReservationRepository reservationRepository;
    private final TrajetMapper trajetMapper;


    @Override
    @Transactional
    public TrajetResponse publish(Long conducteurId, TrajetRequest request) {
        if (!conducteurRepository.existsById(conducteurId)) {
            log.warn("Conducteur introuvable : id={}", conducteurId);
            throw new ResourceNotFoundException("Conducteur introuvable avec l'id " + conducteurId);
        }

        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId()).orElseThrow(() -> {
            log.warn("Véhicule introuvable : id={}", request.vehiculeId());
            return new ResourceNotFoundException("Véhicule introuvable avec l'id " + request.vehiculeId());
        });

        // scenario 8: a driver can only publish with his own vehicles
        if (!vehicule.getConducteur().getId().equals(conducteurId)) {
            log.warn("Publication refusée : le véhicule {} n'appartient pas au conducteur {}", vehicule.getId(), conducteurId);
            throw new ForbiddenException("Le véhicule " + vehicule.getId() + " n'appartient pas au conducteur " + conducteurId);
        }

        // scenario 7: seats must not exceed the vehicle capacity
        if (request.placesProposees() > vehicule.getCapacite()) {
            throw invalid("Le nombre de places proposées (" + request.placesProposees() + ") dépasse la capacité du véhicule (" + vehicule.getCapacite() + ")");
        }

        validateStops(request.pointsArret());

        Trajet saved = trajetRepository.save(buildTrajet(vehicule, request));
        log.info("Trajet publié : id={}, {} -> {}, conducteurId={}", saved.getId(), saved.getVilleDepart(), saved.getVilleArrivee(), conducteurId);
        return toResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public TrajetResponse getDetails(Long trajetId) {
        Trajet trajet = trajetRepository.findDetailById(trajetId).orElseThrow(() -> {
            log.warn("Trajet introuvable : id={}", trajetId);
            return new ResourceNotFoundException("Trajet introuvable avec l'id " + trajetId);
        });
        return toResponse(trajet);
    }


    @Override
    @Transactional(readOnly = true)
    public List<TrajetReservationResponse> getReservations(Long conducteurId, Long trajetId) {
        Trajet trajet = findTrajet(trajetId);
        checkOwner(trajet, conducteurId);

        return reservationRepository.findByTrajetIdWithDetails(trajetId).stream().map(trajetMapper::toReservationResponse).toList();
    }


    @Override
    @Transactional
    public TrajetResponse complete(Long conducteurId, Long trajetId) {
        Trajet trajet = findTrajet(trajetId);
        checkOwner(trajet, conducteurId);
        ensureNotFinal(trajet);

        // scenario 9: only after the departure time
        if (LocalDateTime.now().isBefore(trajet.getDateDepart())) {
            log.warn("Terminaison refusée : le trajet {} n'a pas encore commencé ({})", trajetId, trajet.getDateDepart());
            throw new BusinessRuleException("Impossible de terminer le trajet avant son heure de départ (" + trajet.getDateDepart() + ")");
        }

        trajet.setStatut(StatutTrajet.TERMINE);
        Trajet saved = trajetRepository.save(trajet);
        log.info("Trajet terminé : id={}, conducteurId={}", trajetId, conducteurId);
        return toResponse(saved);
    }


    @Override
    @Transactional
    public void cancel(Long conducteurId, Long trajetId) {
        Trajet trajet = findTrajet(trajetId);
        checkOwner(trajet, conducteurId);
        ensureNotFinal(trajet);

        // scenario 11: the trip and its reservations succeed or fail together
        List<Reservation> confirmed = reservationRepository.findByTrajetIdAndStatut(trajetId, StatutReservation.CONFIRMEE);
        confirmed.forEach(Reservation::annuler);
        reservationRepository.saveAll(confirmed);

        trajet.setStatut(StatutTrajet.ANNULE);
        trajetRepository.save(trajet);

        log.info("Trajet annulé : id={}, réservations annulées={}", trajetId, confirmed.size());
    }


    private Trajet buildTrajet(Vehicule vehicule, TrajetRequest request) {
        List<PointArretRequest> stops = request.pointsArret();
        PointArretRequest first = stops.get(0);
        PointArretRequest last = stops.get(stops.size() - 1);

        Trajet trajet = Trajet.builder().vehicule(vehicule).villeDepart(first.ville().trim())          // first stop = departure
                .villeArrivee(last.ville().trim())          // last stop = arrival
                .dateDepart(first.heurePassage()).placesProposees(request.placesProposees()).statut(StatutTrajet.PUBLIE).build();

        for (int i = 0; i < stops.size(); i++) {
            PointArretRequest stop = stops.get(i);
            PointArret point = trajetMapper.toEntity(stop);
            point.setOrdre(i);                                              // 0 = departure
            point.setVille(stop.ville().trim());
            point.setPrixDepuisDepart(stop.prixDepuisDepart().setScale(2, RoundingMode.HALF_UP));
            trajet.addPointArret(point);                                    // sets point.trajet
        }
        return trajet;
    }

    private void validateStops(List<PointArretRequest> stops) {
        if (stops == null || stops.size() < 2) {
            throw invalid("Un trajet doit avoir au moins deux points d'arrêt");
        }

        if (!stops.get(0).heurePassage().isAfter(LocalDateTime.now())) {
            throw invalid("Le départ doit être dans le futur");
        }

        long distinctCities = stops.stream().map(s -> s.ville().trim().toLowerCase()).distinct().count();
        if (distinctCities != stops.size()) {
            throw invalid("Une ville ne peut apparaître qu'une seule fois sur le parcours");
        }

        if (stops.get(0).prixDepuisDepart().compareTo(BigDecimal.ZERO) != 0) {
            throw invalid("Le prix depuis le départ doit valoir 0 au premier point");
        }

        for (int i = 1; i < stops.size(); i++) {
            PointArretRequest previous = stops.get(i - 1);
            PointArretRequest current = stops.get(i);

            if (!current.heurePassage().isAfter(previous.heurePassage())) {
                throw invalid("Les heures de passage doivent être strictement croissantes (point " + i + " : " + current.ville() + ")");
            }
            if (current.prixDepuisDepart().compareTo(previous.prixDepuisDepart()) <= 0) {
                throw invalid("Les prix depuis le départ doivent être strictement croissants (point " + i + " : " + current.ville() + ")");
            }
        }
    }

    private Trajet findTrajet(Long id) {
        return trajetRepository.findById(id).orElseThrow(() -> {
            log.warn("Trajet introuvable : id={}", id);
            return new ResourceNotFoundException("Trajet introuvable avec l'id " + id);
        });
    }

    private void checkOwner(Trajet trajet, Long conducteurId) {
        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            log.warn("Accès refusé : le conducteur {} n'est pas propriétaire du trajet {}", conducteurId, trajet.getId());
            throw new ForbiddenException("Le trajet " + trajet.getId() + " n'appartient pas au conducteur " + conducteurId);
        }
    }

    private void ensureNotFinal(Trajet trajet) {
        if (trajet.estFinal()) {
            log.warn("Modification refusée : le trajet {} est déjà {}", trajet.getId(), trajet.getStatut());
            throw new BusinessRuleException("Le trajet " + trajet.getId() + " est déjà " + trajet.getStatut() + " : aucune modification possible");
        }
    }

    // remaining seats are calculated, never stored
    private int remainingSeats(Trajet trajet) {
        Long booked = reservationRepository.sumPlacesByTrajetAndStatut(trajet.getId(), StatutReservation.CONFIRMEE);
        return trajet.getPlacesProposees() - booked.intValue();
    }

    private TrajetResponse toResponse(Trajet trajet) {
        return trajetMapper.toResponse(trajet, remainingSeats(trajet));
    }

    private InvalidRequestException invalid(String message) {
        log.warn("Publication refusée : {}", message);
        return new InvalidRequestException(message);
    }
}