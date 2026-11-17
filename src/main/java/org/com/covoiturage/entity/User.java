package org.com.covoiturage.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor @SuperBuilder
public abstract class User {

 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable = false) private String nom;
 @Column(nullable = false) private String prenom;
 @Column(nullable = false, unique = true) private String email;
 @Column(nullable = false) private String telephone;
}