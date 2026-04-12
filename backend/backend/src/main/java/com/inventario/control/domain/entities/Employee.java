package com.inventario.control.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "odoo_user_id", nullable = false, unique = true)
    private Integer odooUserId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "secreta_totp")
    private String secretaTotp;

    @Enumerated(EnumType.STRING)
    private Role rol;

    private Boolean activo = true;
}