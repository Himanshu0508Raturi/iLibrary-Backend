package com.DBMS.iLibrary.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)// Auto assign id
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Username cannot be blank")
    // @NotBlank for text input , Use @NotEmpty for collections, arrays, or strings where any non-null, non-empty value is acceptable, including whitespace strings.
    //@Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{5,29}$", message = "username must start with a letter subsequent characters can be letters, digits, or underscore")
    private String username;

    @Column(nullable = false, length = 225)
    @NotBlank(message = "Password cannot be blank.")
    private String password;

    @Column(nullable = false, unique = true, length = 150)
    //@Email(message = "Email must be valid.")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(gmail|outlook)\\.com$", message = "Enter must be valid.")
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    //Tells JPA: “This field isn’t a separate entity, but a collection of simple values (like Strings, Integers, Embeddable).”
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    //Defines the table name where this collection will be stored.
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();// STUDENT, LIBRARIAN, ADMIN

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt = new Date();
}
