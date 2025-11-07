package org.example.wealthflow.user.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", indexes = {
        @Index(columnList = "login"),
        @Index(columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    public enum Role { USER, ADMIN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @NotBlank(message = "Login cannot be blank")
    @Column(name = "login", unique = true, nullable = false, length = 100)
    private String login;

    @NotBlank(message = "Password hash cannot be blank")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank(message = "Salt cannot be blank")
    @Column(name = "salt", nullable = false, length = 255)
    private String salt;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be blank")
    @Column(name ="email", unique = true, length = 255)
    private String email;

    @NotBlank(message = "Full name cannot be blank")
    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
}
