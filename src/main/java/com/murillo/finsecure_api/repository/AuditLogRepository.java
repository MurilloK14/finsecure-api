package com.murillo.finsecure_api.repository;

import com.murillo.finsecure_api.domain.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByEntityNameAndEntityId(String entityName, String entityId);
    List<AuditLog> findByUserEmail(String userEmail);
}
