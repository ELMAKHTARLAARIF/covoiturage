package org.com.covoiturage.DTOs;

import jakarta.validation.constraints.*;

public record VehiculeRequest(
        @NotBlank(message = "La marque est obligatoire") String marque,
        @NotBlank(message = "Le modèle est obligatoire") String modele,
        @NotBlank(message = "L'immatriculation est obligatoire") String immatriculation,
        String couleur,
        @NotNull(message = "La capacité est obligatoire")
        @Min(value = 1, message = "La capacité minimale est de 1 place")
        @Max(value = 8, message = "La capacité maximale est de 8 places")
        Integer capacite
) {}