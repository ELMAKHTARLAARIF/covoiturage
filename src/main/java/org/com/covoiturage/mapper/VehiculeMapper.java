package org.com.covoiturage.mapper;

import org.com.covoiturage.DTOs.VehiculeRequest;
import org.com.covoiturage.DTOs.VehiculeResponse;
import org.com.covoiturage.entity.Vehicule;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    @Mapping(target = "immatriculation", ignore = true)
    Vehicule toEntity(VehiculeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur", ignore = true)
    void update(VehiculeRequest request, @MappingTarget Vehicule vehicule);

    @Mapping(target = "conducteurId", source = "conducteur.id")
    VehiculeResponse toResponse(Vehicule vehicule);
}