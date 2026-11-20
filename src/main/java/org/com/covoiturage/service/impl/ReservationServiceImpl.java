package org.com.covoiturage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.covoiturage.DTOs.Reservation.ReservationRequest;
import org.com.covoiturage.DTOs.Reservation.ReservationResultResponse;
import org.com.covoiturage.entity.Passager;
import org.com.covoiturage.entity.PointArret;
import org.com.covoiturage.entity.Reservation;
import org.com.covoiturage.entity.Trajet;
import org.com.covoiturage.enums.StatutReservation;
import org.com.covoiturage.enums.StatutTrajet;
import org.com.covoiturage.exception.BusinessRuleException;
import org.com.covoiturage.exception.ForbiddenException;
import org.com.covoiturage.exception.InvalidRequestException;
import org.com.covoiturage.exception.ResourceNotFoundException;
import org.com.covoiturage.mapper.ReservationMapper;
import org.com.covoiturage.repository.PassagerRepository;
import org.com.covoiturage.repository.ReservationRepository;
import org.com.covoiturage.repository.TrajetRepository;
import org.com.covoiturage.service.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final PassagerRepository passagerRepository;
    private final ReservationMapper reservationMapper;

    @Override
    @Transactional
    public ReservationResultResponse reserve(Long passagerId, ReservationRequest request) {

        //  the passenger and the trip must exist (the trip row is locked)

        Passager passager = passagerRepository.findById(passagerId).orElseThrow(() -> {
            log.warn("Passager Not Found:{}", passagerId);
            return new ResourceNotFoundException("Passager Not Found");
        });

        Trajet trajet = trajetRepository.findById(request.trajetId()).orElseThrow(() -> {
            log.warn("Trip Not Found With ID:{}", request.trajetId());
            return new ResourceNotFoundException("Trip Not Found");
        });

        // selection Casa->Marrakech check Casa point wch kayna fhad trajet also check Marrakech
        PointArret pointMontee = findStop(trajet,request.pointMonteeId());
        PointArret pointDescente = findStop(trajet, request.pointDescenteId());

        // ordre dyal point montee >= point  Descente (Marrakech -> Casa !!!)
        if (pointMontee.getOrdre() >= pointDescente.getOrdre()) {
            log.warn("Réservation refusée : montée ({}) après la descente ({})", pointMontee.getVille(), pointDescente.getVille());
            throw new InvalidRequestException("Le point de montée (" + pointMontee.getVille() + ") doit être avant le point de descente (" + pointDescente.getVille() + ")");
        }

        //  only a PUBLIE trip can be booked
        if (trajet.getStatut() != StatutTrajet.PUBLIE) {
            throw new BusinessRuleException("Le trajet " + trajet.getId() + " n'est pas ouvert à la réservation (statut : " + trajet.getStatut() + ")");
        }

        // 6. one CONFIRMEE reservation per passenger per trip
        if (reservationRepository.hasReservation(passagerId,trajet.getId(),StatutReservation.CONFIRMEE)){
            throw new BusinessRuleException("You Already Have This Reservation");
        }

        // place reserve kber mn place restant !!  (placePropose - booked from reservation confirme)
        int remaining = remainingSeats(trajet);
        if (request.nombrePlaces() > remaining) {
            throw new BusinessRuleException("Places insuffisantes : " + request.nombrePlaces() + " demandées, " + remaining + " restantes");
        }

        // total = (drop-off price - boarding price) x seats
        BigDecimal pricePerSeat = pointMontee.prixVers(pointDescente);
        BigDecimal total = pricePerSeat.multiply(BigDecimal.valueOf(request.nombrePlaces())).setScale(2, RoundingMode.HALF_UP);

        //  save
        Reservation reservation = Reservation.builder().passager(passager).pointMontee(pointMontee).pointDescente(pointDescente).nombrePlaces(request.nombrePlaces()).montantTotal(total).dateReservation(LocalDateTime.now()).statut(StatutReservation.CONFIRMEE).build();
        Reservation saved = reservationRepository.save(reservation);
        //lblays li b9aw n9es mnhom lblays li treservaw db, ila kan 0 mab9awch lblays atrje3 status trajet complet
        int seatsLeft = remaining - request.nombrePlaces();
        if (seatsLeft == 0) {
            trajet.setStatut(StatutTrajet.COMPLET);
            trajetRepository.save(trajet);
            log.info("Trajet {} est maintenant COMPLET", trajet.getId());
        }

        log.info("Réservation créée : id={}, passagerId={}, trajetId={}, places={}, montant={}", saved.getId(), passagerId, trajet.getId(), saved.getNombrePlaces(), total);
        return reservationMapper.toResultResponse(saved, seatsLeft);
    }


    @Override
    @Transactional
    public void cancel(Long passagerId, Long reservationId) {

        //the reservation must exist
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> {
            log.warn("Réservation introuvable : id={}", reservationId);
            return new ResourceNotFoundException("Réservation introuvable avec l'id " + reservationId);
        });

        // it must belong to this passenger
        if (!reservation.getPassager().getId().equals(passagerId)) {
            log.warn("Accès refusé : le passager {} n'est pas propriétaire de la réservation {}", passagerId, reservationId);
            throw new ForbiddenException("La réservation " + reservationId + " n'appartient pas au passager " + passagerId);
        }

        // it must still be CONFIRMEE
        if (!reservation.estConfirmee()) {
            throw new BusinessRuleException("La réservation " + reservationId + " est déjà annulée");
        }

        //at least 24 hours before departure (scenario 10)
        Trajet trajet = trajetRepository.findByIdForUpdate(reservation.getTrajet().getId()).orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable"));
        LocalDateTime lastCancelTime = trajet.getDateDepart().minusHours(24);
        if (LocalDateTime.now().isAfter(lastCancelTime)) {
            throw new BusinessRuleException("Annulation impossible : le délai de 24 h avant le départ est dépassé");
        }

        //cancel, and reopen the trip if it was full
        reservation.annuler();
        reservationRepository.save(reservation);

        if (trajet.getStatut() == StatutTrajet.COMPLET) {
            trajet.setStatut(StatutTrajet.PUBLIE);
            trajetRepository.save(trajet);
            log.info("Trajet {} est de nouveau PUBLIE", trajet.getId());
        }

        log.info("Réservation annulée : id={}, passagerId={}", reservationId, passagerId);
    }
    

    // finds a stop inside the trip's own list, so a foreign stop is rejected
    private PointArret findStop(Trajet trajet, Long pointId) {
            return trajet.getPointsArret().stream().filter(p->p.getId().equals(pointId)).findFirst().orElseThrow(()->{
                 log.warn("Break Point  Not In This Trajet : id={}", pointId);
                 return new ResourceNotFoundException("Break Point  Not In This Trajet");
            })  ;
    }

    // remaining seats = offered seats - seats of CONFIRMEE reservations (calculated, never stored)
    private int remainingSeats(Trajet trajet) {
        Long booked = reservationRepository.sumPlacesByTrajetAndStatut(trajet.getId(), StatutReservation.CONFIRMEE);
        return trajet.getPlacesProposees() - booked.intValue();
    }

}