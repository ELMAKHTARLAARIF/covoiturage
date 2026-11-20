package org.com.covoiturage.DTOs.Trajet;

import org.com.covoiturage.DTOs.PointArret.PointArretResponse;
import org.com.covoiturage.enums.StatutTrajet;

import java.time.LocalDateTime;
import java.util.List;

public record TrajetResponse(
        Long id,
        Long conducteurId, String conducteurNom, String conducteurPrenom,
        Long vehiculeId, String vehiculeMarque, String vehiculeModele,
        String villeDepart, String villeArrivee, LocalDateTime dateDepart,
        int placesProposees, int placesRestantes,
        StatutTrajet statut,
        List<PointArretResponse> pointsArret
) {}