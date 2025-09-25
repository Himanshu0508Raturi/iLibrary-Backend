package com.DBMS.iLibrary.entity;

import jakarta.persistence.*;
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
    private String username;

    @Column(nullable = false, length = 225)
    private String password;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER) //Tells JPA: “This field isn’t a separate entity, but a collection of simple values (like Strings, Integers, Embeddables).”
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id")) //Defines the table name where this collection will be stored.
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();// STUDENT, LIBRARIAN, ADMIN

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt = new Date();
}
