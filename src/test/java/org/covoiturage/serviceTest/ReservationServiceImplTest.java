package org.covoiturage.serviceTest;

import org.com.covoiturage.DTOs.Reservation.ReservationRequest;
import org.com.covoiturage.DTOs.Reservation.ReservationResultResponse;
import org.com.covoiturage.entity.*;
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
import org.com.covoiturage.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {
    // create fake objects
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TrajetRepository trajetRepository;
    @Mock
    private PassagerRepository passagerRepository;
    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    // Hamid's trip: Rabat(1) -> Casablanca(2) -> Settat(3) -> Marrakech(4), 3 seats offered
    private Passager salma;
    private Passager karim;
    private Trajet trajet;
    private PointArret rabat;
    private PointArret casablanca;
    private PointArret settat;
    private PointArret marrakech;

    @BeforeEach
    void setUp() {

        // create passagers
        salma = Passager.builder().id(2L).nom("Bennani").prenom("Salma").email("salma@mail.com").telephone("0612345678").build();
        karim = Passager.builder().id(3L).nom("Tazi").prenom("Karim").email("karim@mail.com").telephone("0623456789").build();

        //create Conducteur
        Conducteur hamid = Conducteur.builder().id(1L).nom("Alami").prenom("Hamid").email("hamid@mail.com").telephone("0612345679").numeroPermis("P-123456").build();
        // create Vehicule
        Vehicule logan = Vehicule.builder().id(1L).marque("Dacia").modele("Logan").immatriculation("12345-A-1").capacite(4).conducteur(hamid).build();
        //   create trajet 
        trajet = Trajet.builder().id(1L).vehicule(logan).villeDepart("Rabat").villeArrivee("Marrakech").dateDepart(LocalDateTime.now().plusDays(3)).placesProposees(3).statut(StatutTrajet.PUBLIE).build();
       // create pointArret
        rabat = stop(1L, "Rabat", 0, "0.00");
        casablanca = stop(2L, "Casablanca", 1, "40.00");
        settat = stop(3L, "Settat", 2, "70.00");
        marrakech = stop(4L, "Marrakech", 3, "120.00");
        trajet.addPointArret(rabat);        // also sets stop.trajet
        trajet.addPointArret(casablanca);
        trajet.addPointArret(settat);
        trajet.addPointArret(marrakech);
    }

    private PointArret stop(Long id, String ville, int ordre, String prix) {
        return PointArret.builder().id(id).ville(ville).ordre(ordre).heurePassage(LocalDateTime.now().plusDays(3).plusHours(ordre)).prixDepuisDepart(new BigDecimal(prix)).build();
    }

    private Reservation confirmedReservation(Passager passager) {
        return Reservation.builder().id(10L).passager(passager).pointMontee(settat).pointDescente(marrakech).nombrePlaces(1).montantTotal(new BigDecimal("50.00")).dateReservation(LocalDateTime.now()).statut(StatutReservation.CONFIRMEE).build();
    }

    private void givenPassagerAndTrajetExist(Passager passager) {
        when(passagerRepository.findById(passager.getId())).thenReturn(Optional.of(passager));
        when(trajetRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(trajet));
    }

    private void givenNoPreviousReservation(Passager passager) {
        when(reservationRepository.hasReservation(passager.getId(), 1L, StatutReservation.CONFIRMEE)).thenReturn(false);
    }

    private void givenBookedSeats(long booked) {
        when(reservationRepository.sumPlacesByTrajetAndStatut(1L, StatutReservation.CONFIRMEE)).thenReturn(booked);
    }

    private void givenSaveReturnsSameReservation() {
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private ReservationResultResponse fakeResponse(int seatsLeft, StatutTrajet statutTrajet) {
        return new ReservationResultResponse(10L, 1L, "Settat", "Marrakech", 1, BigDecimal.ZERO, LocalDateTime.now(), StatutReservation.CONFIRMEE, seatsLeft, statutTrajet);
    }

    @Nested
    @DisplayName("reserve()")
    class Reserve {

        @Test
        @DisplayName("Scenario 1: Salma books 1 seat Settat -> Marrakech = 50 DH, 2 seats left")
        void shouldBookSeatAndCalculateAmount() {
            givenPassagerAndTrajetExist(salma);
            givenNoPreviousReservation(salma);
            givenBookedSeats(0L);
            givenSaveReturnsSameReservation();
            when(reservationMapper.toResultResponse(any(Reservation.class), eq(2))).thenReturn(fakeResponse(2, StatutTrajet.PUBLIE));

            ReservationResultResponse result = reservationService.reserve(2L, new ReservationRequest(1L, 3L, 4L, 1));

            ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).save(captor.capture());
            Reservation saved = captor.getValue();

            assertThat(saved.getMontantTotal()).isEqualByComparingTo("50.00");   // (120 - 70) x 1
            assertThat(saved.getNombrePlaces()).isEqualTo(1);
            assertThat(saved.getStatut()).isEqualTo(StatutReservation.CONFIRMEE);
            assertThat(saved.getPassager()).isEqualTo(salma);
            assertThat(result.placesRestantes()).isEqualTo(2);
            assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.PUBLIE);       // still open
            verify(trajetRepository, never()).save(any(Trajet.class));
        }

        @Test
        @DisplayName("Scenario 3: Karim books the last 2 seats = 160 DH, trip becomes COMPLET")
        void shouldMakeTripCompletWhenLastSeatsAreBooked() {
            givenPassagerAndTrajetExist(karim);
            givenNoPreviousReservation(karim);
            givenBookedSeats(1L);                       // Salma already has 1 seat
            givenSaveReturnsSameReservation();
            when(reservationMapper.toResultResponse(any(Reservation.class), eq(0))).thenReturn(fakeResponse(0, StatutTrajet.COMPLET));

            ReservationResultResponse result = reservationService.reserve(3L, new ReservationRequest(1L, 2L, 4L, 2));

            ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).save(captor.capture());

            assertThat(captor.getValue().getMontantTotal()).isEqualByComparingTo("160.00"); // (120 - 40) x 2
            assertThat(result.placesRestantes()).isZero();
            assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.COMPLET);
            verify(trajetRepository).save(trajet);
        }

        @Test
        @DisplayName("The amount is rounded to 2 decimals (HALF_UP)")
        void shouldRoundAmountToTwoDecimals() {
            casablanca.setPrixDepuisDepart(new BigDecimal("33.335"));
            givenPassagerAndTrajetExist(salma);
            givenNoPreviousReservation(salma);
            givenBookedSeats(0L);
            givenSaveReturnsSameReservation();
            when(reservationMapper.toResultResponse(any(Reservation.class), eq(0))).thenReturn(fakeResponse(0, StatutTrajet.COMPLET));

            reservationService.reserve(2L, new ReservationRequest(1L, 1L, 2L, 3));   // Rabat -> Casablanca

            ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).save(captor.capture());

            // 33.335 x 3 = 100.005 -> 100.01
            assertThat(captor.getValue().getMontantTotal()).isEqualTo(new BigDecimal("100.01"));
        }

        @Test
        @DisplayName("Scenario 4: booking a COMPLET trip is refused (409)")
        void shouldRefuseWhenTripIsComplet() {
            trajet.setStatut(StatutTrajet.COMPLET);
            givenPassagerAndTrajetExist(salma);

            assertThatThrownBy(() -> reservationService.reserve(2L, new ReservationRequest(1L, 2L, 4L, 1))).isInstanceOf(BusinessRuleException.class).hasMessageContaining("COMPLET");

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Scenario 6: boarding after drop-off is refused (400)")
        void shouldRefuseWhenBoardingIsAfterDropOff() {
            givenPassagerAndTrajetExist(salma);

            // boarding Marrakech (ordre 3), drop-off Settat (ordre 2)
            assertThatThrownBy(() -> reservationService.reserve(2L, new ReservationRequest(1L, 4L, 3L, 1))).isInstanceOf(InvalidRequestException.class).hasMessageContaining("doit être avant le point de descente");

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Boarding and drop-off on the same stop is refused (400)")
        void shouldRefuseWhenBothStopsAreTheSame() {
            givenPassagerAndTrajetExist(salma);

            assertThatThrownBy(() -> reservationService.reserve(2L, new ReservationRequest(1L, 3L, 3L, 1))).isInstanceOf(InvalidRequestException.class);

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("A stop that does not belong to the trip is refused (400)")
        void shouldRefuseWhenStopDoesNotBelongToTrip() {
            givenPassagerAndTrajetExist(salma);

            assertThatThrownBy(() -> reservationService.reserve(2L, new ReservationRequest(1L, 99L, 4L, 1))).isInstanceOf(InvalidRequestException.class).hasMessageContaining("n'appartient pas au trajet");

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("A passenger cannot have two CONFIRMEE reservations on the same trip (409)")
        void shouldRefuseSecondConfirmedReservationOnSameTrip() {
            givenPassagerAndTrajetExist(salma);
            when(reservationRepository.hasReservation(2L, 1L, StatutReservation.CONFIRMEE)).thenReturn(true);

            assertThatThrownBy(() -> reservationService.reserve(2L, new ReservationRequest(1L, 3L, 4L, 1))).isInstanceOf(BusinessRuleException.class).hasMessageContaining("déjà une réservation confirmée");

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Booking more seats than remaining is refused (409)")
        void shouldRefuseWhenNotEnoughSeats() {
            givenPassagerAndTrajetExist(karim);
            givenNoPreviousReservation(karim);
            givenBookedSeats(1L);                       // 3 offered - 1 booked = 2 remaining

            assertThatThrownBy(() -> reservationService.reserve(3L, new ReservationRequest(1L, 2L, 4L, 3))).isInstanceOf(BusinessRuleException.class).hasMessageContaining("Places insuffisantes");

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Unknown passenger -> 404")
        void shouldFailWhenPassagerNotFound() {
            when(passagerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.reserve(99L, new ReservationRequest(1L, 3L, 4L, 1))).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Passager introuvable");

            verifyNoInteractions(trajetRepository);
        }

        @Test
        @DisplayName("Unknown trip -> 404")
        void shouldFailWhenTrajetNotFound() {
            when(passagerRepository.findById(2L)).thenReturn(Optional.of(salma));
            when(trajetRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.reserve(2L, new ReservationRequest(99L, 3L, 4L, 1))).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Trajet introuvable");

            verify(reservationRepository, never()).save(any(Reservation.class));
        }
    }

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("Scenario 5: cancelling 3 days before departure reopens a COMPLET trip")
        void shouldCancelAndReopenCompletTrip() {
            trajet.setStatut(StatutTrajet.COMPLET);
            Reservation reservation = confirmedReservation(salma);
            when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
            when(trajetRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(trajet));

            reservationService.cancel(2L, 10L);

            assertThat(reservation.getStatut()).isEqualTo(StatutReservation.ANNULEE);
            assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
            verify(reservationRepository).save(reservation);
            verify(trajetRepository).save(trajet);
        }

        @Test
        @DisplayName("Cancelling on a PUBLIE trip leaves the trip untouched")
        void shouldCancelWithoutChangingPublishedTrip() {
            Reservation reservation = confirmedReservation(salma);
            when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
            when(trajetRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(trajet));

            reservationService.cancel(2L, 10L);

            assertThat(reservation.getStatut()).isEqualTo(StatutReservation.ANNULEE);
            assertThat(trajet.getStatut()).isEqualTo(StatutTrajet.PUBLIE);
            verify(reservationRepository).save(reservation);
            verify(trajetRepository, never()).save(any(Trajet.class));
        }

        @Test
        @DisplayName("Scenario 10: cancelling 2 hours before departure is refused (409)")
        void shouldRefuseCancellationLessThan24HoursBeforeDeparture() {
            trajet.setDateDepart(LocalDateTime.now().plusHours(2));
            Reservation reservation = confirmedReservation(salma);
            when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
            when(trajetRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(trajet));

            assertThatThrownBy(() -> reservationService.cancel(2L, 10L)).isInstanceOf(BusinessRuleException.class).hasMessageContaining("24 h");

            assertThat(reservation.getStatut()).isEqualTo(StatutReservation.CONFIRMEE);   // unchanged
            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancelling someone else's reservation is refused (403)")
        void shouldRefuseWhenReservationBelongsToAnotherPassenger() {
            Reservation reservation = confirmedReservation(salma);
            when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.cancel(3L, 10L))      // Karim, not Salma
                    .isInstanceOf(ForbiddenException.class).hasMessageContaining("n'appartient pas au passager");

            assertThat(reservation.getStatut()).isEqualTo(StatutReservation.CONFIRMEE);
            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancelling an already cancelled reservation is refused (409)")
        void shouldRefuseWhenAlreadyCancelled() {
            Reservation reservation = confirmedReservation(salma);
            reservation.setStatut(StatutReservation.ANNULEE);
            when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.cancel(2L, 10L)).isInstanceOf(BusinessRuleException.class).hasMessageContaining("déjà annulée");

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Unknown reservation -> 404")
        void shouldFailWhenReservationNotFound() {
            when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.cancel(2L, 99L)).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Réservation introuvable");

            verifyNoInteractions(trajetRepository);
        }
    }
}