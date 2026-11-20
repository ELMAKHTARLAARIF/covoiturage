package org.com.covoiturage.mapper;

import org.com.covoiturage.DTOs.PointArret.PointArretRequest;
import org.com.covoiturage.DTOs.PointArret.PointArretResponse;
import org.com.covoiturage.DTOs.Trajet.TrajetReservationResponse;
import org.com.covoiturage.DTOs.Trajet.TrajetResponse;
import org.com.covoiturage.entity.PointArret;
import org.com.covoiturage.entity.Reservation;
import org.com.covoiturage.entity.Trajet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrajetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trajet", ignore = true)   // set by Trajet.addPointArret()
    @Mapping(target = "ordre", ignore = true)    // set by the service from the list position
    PointArret toEntity(PointArretRequest request);

    PointArretResponse toResponse(PointArret pointArret);

    // placesRestantes is calculated by the service, so it comes in as a parameter
    @Mapping(target = "id", source = "trajet.id")
    @Mapping(target = "conducteurId", source = "trajet.conducteur.id")
    @Mapping(target = "conducteurNom", source = "trajet.conducteur.nom")
    @Mapping(target = "conducteurPrenom", source = "trajet.conducteur.prenom")
    @Mapping(target = "vehiculeId", source = "trajet.vehicule.id")
    @Mapping(target = "vehiculeMarque", source = "trajet.vehicule.marque")
    @Mapping(target = "vehiculeModele", source = "trajet.vehicule.modele")
    @Mapping(target = "placesRestantes", source = "placesRestantes")
    TrajetResponse toResponse(Trajet trajet, int placesRestantes);

    @Mapping(target = "passagerId", source = "passager.id")
    @Mapping(target = "passagerNom", source = "passager.nom")
    @Mapping(target = "passagerPrenom", source = "passager.prenom")
    @Mapping(target = "villeMontee", source = "pointMontee.ville")
    @Mapping(target = "villeDescente", source = "pointDescente.ville")
    TrajetReservationResponse toReservationResponse(Reservation reservation);
}