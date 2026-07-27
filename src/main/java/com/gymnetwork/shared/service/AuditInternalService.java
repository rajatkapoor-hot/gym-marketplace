package com.gymnetwork.shared.service;

import java.util.UUID;

public interface AuditInternalService {
    void logAction(UUID userId, String entityName, String entityId, String action, String changes);
}
