package org.com.covoiturage.service;

import org.com.covoiturage.DTOs.ConducteurRequest;
import org.com.covoiturage.DTOs.ConducteurResponse;

import java.util.List;

public interface ConducteurService {
    ConducteurResponse create(ConducteurRequest request);
    List<ConducteurResponse> list();
    ConducteurResponse findById(Long id);
    ConducteurResponse update(Long id, ConducteurRequest request);
    List<ConducteurResponse> search(String nom, String email);
}