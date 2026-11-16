package com.portfolio.wallet.entity;

import jakarta.persistence.*;
import lombok.*;

// "user" is a reserved word in PostgreSQL/H2, so the table is called "users".
@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;
}
