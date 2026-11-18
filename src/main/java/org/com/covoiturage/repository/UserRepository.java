package org.com.covoiturage.repository;

import org.com.covoiturage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, long id);
    boolean existsByEmailIgnoreCase(String email);

}
