package org.com.covoiturage.DTOs.Conducteur;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record ConducteurRequest(
        @NotBlank(message = "le nom est obligatoire") String nom,
        @NotBlank(message = "le prenom est obligatoire") String prenom,
        @NotBlank(message = "L'email est obligatoire") @Email(message = "Format d'email invalide") String email,
        //@Pattern(regexp = "^\\\\+?[0-9 ]{8,15}$", message = "Format de telephone invalide")
        @NotBlank(message = "Le telephone est obligatoire")  String telephone,
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères")
        String password,
        @NotBlank(message = "Le numero de permis est obligatoire ") String numeroPermis) {
}

