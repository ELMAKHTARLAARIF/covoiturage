package org.com.covoiturage.DTOs;

public record VehiculeResume(Long id, String marque, String modele, String immatriculation,
                             String couleur, int capacite) {}