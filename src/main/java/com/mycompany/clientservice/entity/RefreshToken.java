package com.mycompany.clientservice.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @ManyToOne
    private AppUser user;

    private Instant expiryDate;
}
