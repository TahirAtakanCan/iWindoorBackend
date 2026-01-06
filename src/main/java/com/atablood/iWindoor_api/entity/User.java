package com.atablood.iWindoor_api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;       // Giriş için kullanıcı adı

    @Column(nullable = false)
    private String password;    // Şifre (BCrypt ile şifrelenecek)

    private String fullName;    // Ad Soyad

    @Enumerated(EnumType.STRING)
    private Role role;          // COMPANY_ADMIN veya EMPLOYEE

    // Hangi şirkete bağlı?
    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonBackReference
    private Company company;
}