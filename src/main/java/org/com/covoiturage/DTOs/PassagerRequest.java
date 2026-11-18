package org.com.covoiturage.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PassagerRequest(@NotBlank(message = "Le nom est obligatoire") String nom,
                              @NotBlank(message = "Le prenom est obligatoire") String prenom,
                              @NotBlank(message = "L'email est obligatoire") @Email(regexp = "Format d'email invalide") String email,
                              @NotBlank(message = "Le telephone est obligatoire") @Pattern(regexp = "^\\\\+?[0-9 ]{8,15}$", message = "Format de telephone invalide") String telephone,
                              @NotBlank(message = "Le mot de passe est obligatoire")
                              @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères")
                              String password) {

}