package com.ecoloop.audit;

import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {
=======

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
>>>>>>> 91b8f441aff11a409c373366a84eaa9de4b4e26c
}
