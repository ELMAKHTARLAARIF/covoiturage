package org.com.covoiturage.mapper;

import org.com.covoiturage.DTOs.Conducteur.ConducteurRequest;
import org.com.covoiturage.DTOs.Conducteur.ConducteurResponse;
import org.com.covoiturage.DTOs.Passager.PassagerRequest;
import org.com.covoiturage.DTOs.Passager.PassagerResponse;
import org.com.covoiturage.entity.Conducteur;
import org.com.covoiturage.entity.Passager;
import org.mapstruct.*;

@Mapper(componentModel = "spring")

public interface UserMapper {
    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    Conducteur toConducteur(ConducteurRequest request);

    @Mapping(target = "id",ignore = true)
    void updateConducteur(ConducteurRequest request,@MappingTarget Conducteur  response);

    ConducteurResponse toConducteurResponse(Conducteur conducteur);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id",ignore = true)
    @Mapping(target = "password", ignore = true)
    Passager toPassager(PassagerRequest request);

    @Mapping(target = "id",ignore = true)
    void updatePassager(Passager passager,@MappingTarget Passager response);

    PassagerResponse toPassagerResponse(Passager passager);

}
