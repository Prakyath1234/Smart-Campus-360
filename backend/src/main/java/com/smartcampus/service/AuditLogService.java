package com.smartcampus.service;

import com.smartcampus.dto.AuditLogDto;
import com.smartcampus.entity.AuditLog;
import com.smartcampus.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(String actorEmail, String actorRole, String action, String entityType, Long entityId, String description, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .actorEmail(actorEmail != null ? actorEmail : "SYSTEM")
                .actorRole(actorRole != null ? actorRole : "SYSTEM")
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .build();
        auditLogRepository.save(log);
    }

    public Page<AuditLogDto> getAuditLogs(String actor, String action, String entityType, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (actor != null && !actor.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("actorEmail")), "%" + actor.trim().toLowerCase() + "%"));
            }

            if (action != null && !action.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("action")), action.trim().toUpperCase()));
            }

            if (entityType != null && !entityType.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("entityType")), entityType.trim().toUpperCase()));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    private AuditLogDto mapToDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .actorEmail(log.getActorEmail())
                .actorRole(log.getActorRole())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
