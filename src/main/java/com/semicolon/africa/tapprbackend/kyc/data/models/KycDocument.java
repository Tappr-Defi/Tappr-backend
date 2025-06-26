package com.semicolon.africa.tapprbackend.kyc.data.models;

import com.semicolon.africa.tapprbackend.kyc.enums.DocumentType;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "kyc_documents")
public class KycDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private DocumentType type;
    
    @Column(nullable = false)
    private String documentUrl;
    
    @Column(nullable = false)
    private boolean verified = false;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
