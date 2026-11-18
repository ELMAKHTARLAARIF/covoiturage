package org.com.covoiturage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.covoiturage.DTOs.ConducteurRequest;
import org.com.covoiturage.DTOs.ConducteurResponse;
import org.com.covoiturage.entity.Conducteur;
import org.com.covoiturage.exception.BusinessRuleException;
import org.com.covoiturage.exception.InvalidRequestException;
import org.com.covoiturage.exception.ResourceNotFoundException;
import org.com.covoiturage.mapper.UserMapper;
import org.com.covoiturage.repository.ConducteurRepository;
import org.com.covoiturage.repository.UserRepository;
import org.com.covoiturage.service.ConducteurService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConducteurServiceImpl implements ConducteurService {

    private final ConducteurRepository conducteurRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public ConducteurResponse create(ConducteurRequest request) {
        verifierUnicite(request, null);

        Conducteur conducteur = userMapper.toConducteur(request);
        conducteur.setPassword(passwordEncoder.encode(request.password()));
        Conducteur saved = conducteurRepository.save(conducteur);
        log.info("Conducteur saved: id={},email={}", saved.getId(), saved.getEmail());// show info log in console
        return userMapper.toConducteurResponse(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponse> list() {
        return conducteurRepository.findAll().stream().map(userMapper::toConducteurResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConducteurResponse findById(Long id) {
        return userMapper.toConducteurResponse(trouver(id));
    }


    @Override
    @Transactional
    public ConducteurResponse update(Long id, ConducteurRequest request) {
        Conducteur conducteur = trouver(id);
        verifierUnicite(request, id);
        String ancienHash = conducteur.getPassword();
        userMapper.updateConducteur(request, conducteur);

        if (request.password() != null && !request.password().isBlank()) {
            conducteur.setPassword(passwordEncoder.encode(request.password()));
            log.info("Mot de passe conducteur modifié : id={}", id);
        } else {
            conducteur.setPassword(ancienHash);
        }

        log.info("Profil conducteur modifié : id={}", id);
        return userMapper.toConducteurResponse(conducteurRepository.save(conducteur));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponse> search(String nom, String email) {
        List<Conducteur> resultat;
        if (email != null && !email.isBlank()) {
            resultat = conducteurRepository.findByEmailContainingIgnoreCase(email.trim());
        } else if (nom != null && !nom.isBlank()) {
            String terme = nom.trim();
            resultat = conducteurRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(terme, terme);
        } else {
            log.warn("Recherche conducteur refusée : aucun critère");
            throw new InvalidRequestException("Renseignez le paramètre 'nom' ou 'email'");
        }
        return resultat.stream().map(userMapper::toConducteurResponse).toList();
    }

    private Conducteur trouver(Long id) {
        return conducteurRepository.findById(id).orElseThrow(() -> {
            log.warn("Conducteur Doesn't exist : id={}", id);
            return new ResourceNotFoundException("Conducteur Doesn't exist with id " + id);
        });
    }


    private void verifierUnicite(ConducteurRequest request, Long id) {
        boolean emailPris = (id == null) ? userRepository.existsByEmailIgnoreCase(request.email()) : userRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id);
        if (emailPris) {
            log.warn("Email Already exist : {}", request.email());
            throw new BusinessRuleException("This Email Already exist : " + request.email());
        }
        boolean permisPris = (id == null) ? conducteurRepository.existsByNumeroPermis(request.numeroPermis()) : conducteurRepository.existsByNumeroPermisAndIdNot(request.numeroPermis(), id);
        if (permisPris) {
            log.warn("Number Already Exist : {}", request.numeroPermis());
            throw new BusinessRuleException("Number Already Exist : " + request.numeroPermis());
        }

    }

    
}
