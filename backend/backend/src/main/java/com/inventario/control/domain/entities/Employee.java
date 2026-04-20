package com.inventario.control.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "odoo_user_id", unique = true)
    private Integer odooUserId;

    @Column(name = "odoo_employee_id", unique = true)
    private Integer odooEmployeeId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_app", nullable = false)
    private String passwordApp;

    @Column(name = "secreta_totp", nullable = false)
    private String secretaTotp;

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "zone_name", nullable = false)
    private String zoneName;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "avatar_color", nullable = false)
    private String avatarColor;

    @Enumerated(EnumType.STRING)
    private Role rol;

    private Boolean activo = true;
}
