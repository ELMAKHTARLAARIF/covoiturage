package org.com.covoiturage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.covoiturage.DTOs.PointArret.PointArretRequest;
import org.com.covoiturage.DTOs.Trajet.TrajetRequest;
import org.com.covoiturage.DTOs.Trajet.TrajetReservationResponse;
import org.com.covoiturage.DTOs.Trajet.TrajetResponse;
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
            log.warn("Driver not found: id={}", conducteurId);
            throw new ResourceNotFoundException("Driver not found with id " + conducteurId);
        }

        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId()).orElseThrow(() -> {
            log.warn("Vehicle not found: id={}", request.vehiculeId());
            return new ResourceNotFoundException("Vehicle not found with id " + request.vehiculeId());
        });

        // Scenario 8: A driver can only publish a trip using their own vehicles
        if (!vehicule.getConducteur().getId().equals(conducteurId)) {
            log.warn("Publication denied: vehicle {} does not belong to driver {}", vehicule.getId(), conducteurId);
            throw new ForbiddenException("Vehicle " + vehicule.getId() + " does not belong to driver " + conducteurId);
        }

        // Scenario 7: The number of proposed seats must not exceed vehicle capacity
        if (request.placesProposees() > vehicule.getCapacite()) {
            throw invalid("The number of proposed seats (" + request.placesProposees() + ") exceeds vehicle capacity (" + vehicule.getCapacite() + ")");
        }

        validateStops(request.pointsArret());

        Trajet saved = trajetRepository.save(buildTrajet(vehicule, request));
        log.info("Trip published: id={}, {} -> {}, driverId={}", saved.getId(), saved.getVilleDepart(), saved.getVilleArrivee(), conducteurId);
        return toResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public TrajetResponse getDetails(Long trajetId) {
        Trajet trajet = trajetRepository.findDetailById(trajetId).orElseThrow(() -> {
            log.warn("Trip not found: id={}", trajetId);
            return new ResourceNotFoundException("Trip not found with id " + trajetId);
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

        // Scenario 9: A trip can only be completed after its departure time (!completed 9bel ma tbda !!!)
        if (LocalDateTime.now().isBefore(trajet.getDateDepart())) {
            log.warn("Completion denied: trip {} has not started yet ({})", trajetId, trajet.getDateDepart());
            throw new BusinessRuleException("Cannot complete the trip before its departure time (" + trajet.getDateDepart() + ")");
        }

        trajet.setStatut(StatutTrajet.TERMINE);
        Trajet saved = trajetRepository.save(trajet);
        log.info("Trip completed: id={}, driverId={}", trajetId, conducteurId);
        return toResponse(saved);
    }


    @Override
    @Transactional
    public void cancel(Long conducteurId, Long trajetId) {
        Trajet trajet = findTrajet(trajetId);
        checkOwner(trajet, conducteurId);
        ensureNotFinal(trajet);

        // Scenario 11: The trip and its reservations succeed or fail together
        List<Reservation> confirmed = reservationRepository.findByTrajetIdAndStatut(trajetId, StatutReservation.CONFIRMEE);
        confirmed.forEach(r -> r.setStatut(StatutReservation.ANNULEE));  // make the reservation Confirmer to  annuler because Trajet will be cancled
        reservationRepository.saveAll(confirmed); // save all reservation annuler

        trajet.setStatut(StatutTrajet.ANNULE);
        trajetRepository.save(trajet);

        log.info("Trip cancelled: id={}, cancelled reservations={}", trajetId, confirmed.size());
    }


    private Trajet buildTrajet(Vehicule vehicule, TrajetRequest request) {
        List<PointArretRequest> stops = request.pointsArret();
        PointArretRequest first = stops.get(0);
        PointArretRequest last = stops.get(stops.size() - 1);

        Trajet trajet = Trajet.builder().vehicule(vehicule).villeDepart(first.ville().trim()).villeArrivee(last.ville().trim()).dateDepart(first.heurePassage()).placesProposees(request.placesProposees()).statut(StatutTrajet.PUBLIE).build();
        // create pointArret for each city
        for (int i = 0; i < stops.size(); i++) {
            PointArretRequest stop = stops.get(i);
            PointArret point = trajetMapper.toEntity(stop);
            point.setOrdre(i);                                              // 0 = departure
            point.setVille(stop.ville().trim());
            point.setPrixDepuisDepart(stop.prixDepuisDepart().setScale(2, RoundingMode.HALF_UP));
            trajet.addPointArret(point);                                    // Sets point.trajet
        }
        return trajet;
    }

    private void validateStops(List<PointArretRequest> stops) {
        if (stops == null || stops.size() < 2) {
            throw invalid("A trip must have at least two stops");
        }

        if (!stops.get(0).heurePassage().isAfter(LocalDateTime.now())) {
            throw invalid("Departure must be in the future");
        }

        // Each city must be unique, a city cannot appear twice on the route
        long distinctCities = stops.stream().map(s -> s.ville().trim().toLowerCase()).distinct().count();
        if (distinctCities != stops.size()) {
            throw invalid("A city cannot appear more than once on the route");
        }

        if (stops.get(0).prixDepuisDepart().compareTo(BigDecimal.ZERO) != 0) {
            throw invalid("The price from departure must be 0 at the first stop");
        }

        for (int i = 1; i < stops.size(); i++) {
            PointArretRequest previous = stops.get(i - 1);
            PointArretRequest current = stops.get(i);

            if (!current.heurePassage().isAfter(previous.heurePassage())) {
                throw invalid("Stop times must be strictly increasing (stop " + i + ": " + current.ville() + ")");
            }
            if (current.prixDepuisDepart().compareTo(previous.prixDepuisDepart()) <= 0) {
                throw invalid("Prices from departure must be strictly increasing (stop " + i + ": " + current.ville() + ")");
            }
        }
    }

    private Trajet findTrajet(Long id) {
        return trajetRepository.findById(id).orElseThrow(() -> {
            log.warn("Trip not found: id={}", id);
            return new ResourceNotFoundException("Trip not found with id " + id);
        });
    }

    private void checkOwner(Trajet trajet, Long conducteurId) {
        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            log.warn("Access denied: driver {} is not the owner of trip {}", conducteurId, trajet.getId());
            throw new ForbiddenException("Trip " + trajet.getId() + " does not belong to driver " + conducteurId);
        }
    }

    private void ensureNotFinal(Trajet trajet) {
        if (trajet.getStatut() == StatutTrajet.TERMINE || trajet.getStatut() == StatutTrajet.ANNULE) {
            log.warn("Modification denied: trip {} is already {}", trajet.getId(), trajet.getStatut());
            throw new BusinessRuleException("Trip " + trajet.getId() + " is already " + trajet.getStatut() + ": no further modifications are allowed");
        }
    }

    // Remaining seats are calculated, never stored    (المقاعد المتبقية)
    private int remainingSeats(Trajet trajet) {
        Long booked = reservationRepository.sumPlacesByTrajetAndStatut(trajet.getId(), StatutReservation.CONFIRMEE);
        return trajet.getPlacesProposees() - booked.intValue();
    }

    private TrajetResponse toResponse(Trajet trajet) {
        return trajetMapper.toResponse(trajet, remainingSeats(trajet));
    }

    private InvalidRequestException invalid(String message) {
        log.warn("Trip publication denied: {}", message);
        return new InvalidRequestException(message);
    }
}
