package org.com.covoiturage.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "passagers")
@PrimaryKeyJoinColumn(name = "id")
@Getter @Setter @NoArgsConstructor @SuperBuilder
public class Passager extends User {
}