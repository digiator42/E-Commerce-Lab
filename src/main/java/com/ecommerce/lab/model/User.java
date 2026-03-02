package com.ecommerce.lab.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String name;

    @Column(unique = true, nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

    private Integer age;

    private String profilePicture;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @JsonIgnore
    private String resetToken;
    @JsonIgnore
    private LocalDateTime resetTokenExpires;

    @JsonIgnore
    private String totpSecret;

    @Column(nullable = true)
    @JsonIgnore
    private boolean isTotpEnabled = false;

    private Role role;

    private String address;

    private Double storeBalance = 0.0;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @JsonIgnore
    private boolean is2faEnabled = false;
    @JsonIgnore
    private String twoFactorCode;
    @JsonIgnore
    private LocalDateTime twoFactorCodeExpires;

    @JsonIgnore
    private LocalDateTime lastLogin;

}
