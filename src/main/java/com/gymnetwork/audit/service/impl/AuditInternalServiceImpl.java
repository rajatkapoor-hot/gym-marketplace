package com.gymnetwork.audit.service.impl;

import com.gymnetwork.audit.service.AuditService;
import com.gymnetwork.shared.service.AuditInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditInternalServiceImpl implements AuditInternalService {

    private final AuditService auditService;

    @Override
    public void logAction(UUID userId, String entityName, String entityId, String action, String changes) {
        auditService.logAction(userId, entityName, entityId, action, changes);
    }
}
