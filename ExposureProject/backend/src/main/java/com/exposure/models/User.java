package com.exposure.models;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/*
    User model
 */


@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User extends SessionMember {
    private String username; // different from nickname!

    private String password;

    private LocalDateTime createdAt;


    public User(String username, String password) {
        super(username);
        this.username = username;
        this.password = password;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
