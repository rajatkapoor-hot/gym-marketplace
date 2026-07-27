package com.gymnetwork.audit.service;

import java.util.UUID;

public interface AuditService {
    void logAction(UUID userId, String entityName, String entityId, String action, String changes);
}
