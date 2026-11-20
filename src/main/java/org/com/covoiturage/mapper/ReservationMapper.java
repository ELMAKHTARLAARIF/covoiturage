package org.com.covoiturage.mapper;

import org.com.covoiturage.DTOs.Reservation.ReservationResponse;
import org.com.covoiturage.DTOs.Reservation.ReservationResultResponse;
import org.com.covoiturage.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(target = "trajetId" , source = "pointMontee.trajet.id")
    @Mapping(target = "villeMontee", source = "pointMontee.ville")
    @Mapping(target = "villeDescente" , source = "pointDescente.ville")
    ReservationResponse toReservationResponse(Reservation reservation);

    @Mapping(target = "id", source = "reservation.id")
    @Mapping(target = "trajetId", source = "reservation.pointMontee.trajet.id")
    @Mapping(target = "villeMontee", source = "reservation.pointMontee.ville")
    @Mapping(target = "villeDescente", source = "reservation.pointDescente.ville")
    @Mapping(target = "nombrePlaces", source = "reservation.nombrePlaces")
    @Mapping(target = "montantTotal", source = "reservation.montantTotal")
    @Mapping(target = "dateReservation", source = "reservation.dateReservation")
    @Mapping(target = "statut", source = "reservation.statut")
    @Mapping(target = "statutTrajet", source = "reservation.pointMontee.trajet.statut")
    @Mapping(target = "placesRestantes", source = "placesRestantes")
    ReservationResultResponse toResultResponse(Reservation reservation, int placesRestantes);
}
