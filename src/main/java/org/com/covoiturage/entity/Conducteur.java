package org.com.covoiturage.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "conducteurs")
@PrimaryKeyJoinColumn(name = "id")
@Getter @Setter @NoArgsConstructor @SuperBuilder
public class Conducteur extends User {

   @Column(name = "numero_permis", nullable = false, unique = true)
   private String numeroPermis;
}