package com.semicolon.africa.tapprbackend.notification.data;

import com.semicolon.africa.tapprbackend.user.data.models.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class Notification {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
