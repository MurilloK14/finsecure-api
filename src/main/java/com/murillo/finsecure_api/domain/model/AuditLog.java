package com.murillo.finsecure_api.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tb_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

@Column(name = "entity_name", nullable = false, length = 50)
    private String entityName;

@Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;

@Column(nullable = false, length = 150)
    private String action;

@Column(name = "user_email", length = 100)
private String userEmail;

@Column(name = "ip_address", length = 45)
private String ipAddress;

@Column(columnDefinition = "Text")
private String details;

@CreationTimestamp
@Column(name = "created_at",  updatable = false)
private Instant createdAt;
}
