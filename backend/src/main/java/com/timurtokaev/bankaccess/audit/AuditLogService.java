package com.timurtokaev.bankaccess.audit;

import com.timurtokaev.bankaccess.audit.dto.AuditLogResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository
    ) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findLatest() {
        return auditLogRepository.findLatest();
    }
}