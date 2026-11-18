package org.com.covoiturage.repository;

import org.com.covoiturage.entity.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
public interface ConducteurRepository extends JpaRepository<Conducteur, Long>, JpaSpecificationExecutor {
    boolean existsByNumeroPermis(String numeroPermis);
    boolean existsByNumeroPermisAndIdNot(String numeroPermis, Long id);
    List<Conducteur> findByEmailContainingIgnoreCase(String email);
    List<Conducteur> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String email,String prenom);

}

