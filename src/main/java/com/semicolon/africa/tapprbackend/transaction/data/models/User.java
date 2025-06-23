package com.semicolon.africa.tapprbackend.transaction.data.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.vomzersocials.user.enums.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;

    @JsonIgnore
    private String password;

    private Boolean isLoggedIn;

    @Column(nullable = false, unique = true)
    private String suiAddress;

    @Column(name = "public_key")
    private String publicKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;

    @Column(name = "salt")
    private String salt;


    @Column(unique = true)
    private String jwtSubjectHash;

    private int followerCount;
    private int followingCount;

    @Column(name = "like_count")
    private int likeCount = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> userLikes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> mediaList = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime dateOfCreation;

//    @PrePersist
//    protected void onCreate() {
//        this.dateOfCreation = LocalDateTime.now();
//    }


}
