package org.com.covoiturage.DTOs.Vehicule;

public record VehiculeResponse(Long id, String marque, String modele, String immatriculation,
                               String couleur, int capacite, Long conducteurId) {}