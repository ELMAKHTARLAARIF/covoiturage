package org.com.covoiturage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.com.covoiturage.DTOs.*;
import org.com.covoiturage.service.ConducteurService;
import org.com.covoiturage.service.PassagerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs",description = "Conducteurs et Passagers")
public class UserController {

    private final ConducteurService conducteurService;
    private final PassagerService passagerService;


    @PostMapping("/conducteurs")
    @ResponseStatus(HttpStatus.CREATED)

    public ConducteurResponse createConducteur(@Valid @RequestBody ConducteurRequest request){
        return conducteurService.create(request);

    }

    @GetMapping("conducteurs")
    public List<ConducteurResponse> listConducteurs(){
        return conducteurService.list();
    }

    @GetMapping("/conducteurs/search")
    private List<ConducteurResponse> searchConducteurs(@RequestParam(required = false) String nom,@RequestParam(required = false) String email){
        return conducteurService.search(nom,email);
    }

    @GetMapping("conducteurs/{id}")
    private ConducteurResponse getConducteurs(@PathVariable Long id){
        return conducteurService.findById(id);
    }

    @PutMapping("conducteurs/{id}")
    private   ConducteurResponse updateConducteur(@PathVariable Long id,@Valid @RequestBody ConducteurRequest request){
        return conducteurService.update(id,request);
    }

    //PASSAGERS

    @PostMapping("/passagers")
    @ResponseStatus(HttpStatus.CREATED)
    public PassagerResponse CreatePassager(@Valid @RequestBody PassagerRequest request) {
        return passagerService.create(request);
    }

    @GetMapping("/passagers")
    public List<PassagerResponse> listPassagers() {
        return passagerService.list();
    }

    @GetMapping("/passagers/search")
    public PassagerResponse searchPassagerByEmail(@RequestParam String email) {
        return passagerService.searchByEmail(email);
    }

    @GetMapping("/passagers/{id}")
    public PassagerResponse findPassager(@PathVariable Long id) {
        return passagerService.findById(id);
    }

    @PutMapping("/passagers/{id}")
    public PassagerResponse updatePassager(@PathVariable Long id,
                                             @Valid @RequestBody PassagerRequest request) {
        return passagerService.update(id, request);
    }

//    @GetMapping("/passagers/{id}/reservations")
//    public List<ReservationResponse> historicReservations(@PathVariable Long id) {
//        return passagerService.historicReservations(id);
//    }
    
}
