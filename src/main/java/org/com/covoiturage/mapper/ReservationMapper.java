package org.com.covoiturage.mapper;

import org.com.covoiturage.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(target = "trajetId" , source = "pointMontee.trajet.id")
    @Mapping(target = "villeMontee", source = "pointMontee.ville")
    @Mapping(target = "villeDescente" , source = "pointDescente.ville")
    ReservationResponse toReservationResponse(Reservation reservation);
}
