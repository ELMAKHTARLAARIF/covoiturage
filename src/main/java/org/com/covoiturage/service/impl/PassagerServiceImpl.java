package org.com.covoiturage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.covoiturage.DTOs.Passager.PassagerRequest;
import org.com.covoiturage.DTOs.Passager.PassagerResponse;
import org.com.covoiturage.DTOs.Reservation.ReservationResponse;
import org.com.covoiturage.entity.Passager;
import org.com.covoiturage.exception.BusinessRuleException;
import org.com.covoiturage.exception.ResourceNotFoundException;
import org.com.covoiturage.mapper.ReservationMapper;
import org.com.covoiturage.mapper.UserMapper;
import org.com.covoiturage.repository.PassagerRepository;
import org.com.covoiturage.repository.ReservationRepository;
import org.com.covoiturage.repository.UserRepository;
import org.com.covoiturage.service.PassagerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassagerServiceImpl implements PassagerService {

    private final PassagerRepository passagerRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final UserMapper userMapper;
    private final ReservationMapper reservationMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public PassagerResponse create(PassagerRequest request) {
        verifierEmail(request.email(), null);

        Passager passager = userMapper.toPassager(request);
        passager.setPassword(passwordEncoder.encode(request.password()));

        Passager saved = passagerRepository.save(passager);
        log.info("Passager Created successfully : id={}, email={}", saved.getId(), saved.getEmail());
        return userMapper.toPassagerResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassagerResponse> list() {
        return passagerRepository.findAll().stream().map(userMapper::toPassagerResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PassagerResponse findById(Long id) {
        return userMapper.toPassagerResponse(trouver(id));
    }

    @Override
    @Transactional
    public PassagerResponse update(Long id, PassagerRequest request) {
        Passager passager = trouver(id);
        verifierEmail(request.email(), id);

        String ancienHash = passager.getPassword();
        userMapper.updatePassager(passager, passager);

        if (request.password() != null && !request.password().isBlank()) {
            passager.setPassword(passwordEncoder.encode(request.password()));
            log.info("Passer Password updated : id={}", id);
        } else {
            passager.setPassword(ancienHash);
        }

        log.info("Passager Profil Updated: id={}", id);
        return userMapper.toPassagerResponse(passagerRepository.save(passager));
    }

    @Override
    @Transactional(readOnly = true)
    public PassagerResponse searchByEmail(String email) {
        return passagerRepository.findByEmailIgnoreCase(email.trim()).map(userMapper::toPassagerResponse).orElseThrow(() -> {
            log.warn("Passager introuvable : email={}", email);
            return new ResourceNotFoundException("Aucun passager avec l'email " + email);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> historicReservations(Long passagerId) {
        trouver(passagerId);
        return reservationRepository.findHistoricByPassagerId(passagerId).stream().map(reservationMapper::toReservationResponse).toList();
    }

    private Passager trouver(Long id) {
        return passagerRepository.findById(id).orElseThrow(() -> {
            log.warn("Passager introuvable : id={}", id);
            return new ResourceNotFoundException("Passager introuvable avec l'id " + id);
        });
    }

    private void verifierEmail(String email, Long id) {
        boolean pris = (id == null) ? userRepository.existsByEmailIgnoreCase(email) : userRepository.existsByEmailIgnoreCaseAndIdNot(email, id);
        if (pris) {
            log.warn("Email Already exist : {}", email);
            throw new BusinessRuleException("Email Already exist : " + email);
        }
    }
}