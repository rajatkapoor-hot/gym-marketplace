package com.gymnetwork.audit.service.impl;

import com.gymnetwork.audit.entity.AuditLogEntity;
import com.gymnetwork.audit.repository.AuditLogRepository;
import com.gymnetwork.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Override
    @Transactional
    public void logAction(UUID userId, String entityName, String entityId, String action, String changes) {
        AuditLogEntity logEntry = AuditLogEntity.builder()
                .userId(userId)
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .changes(changes)
                .build();
                
        auditLogRepository.save(logEntry);
        log.debug("Audit log saved for {} {}", entityName, entityId);
    }
}
