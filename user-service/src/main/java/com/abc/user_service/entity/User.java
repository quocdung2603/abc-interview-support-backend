package com.abc.user_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 100)
    private String fullName;

    private LocalDate dateOfBirth;

    @Column(length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private Boolean isStudying;

    private Integer eloScore;

    @Enumerated(EnumType.STRING)
    private EloRank eloRank;

    private LocalDateTime createdAt;
}