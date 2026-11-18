package org.com.covoiturage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.com.covoiturage.entity.Passager;

import java.util.Optional;

public interface PassagerRepository extends JpaRepository<Passager,Long>  {
    Optional<Passager> findByEmailIgnoreCase(String email);
}
