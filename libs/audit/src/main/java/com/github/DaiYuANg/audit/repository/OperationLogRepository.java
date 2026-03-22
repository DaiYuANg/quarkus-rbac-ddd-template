package com.github.DaiYuANg.audit.repository;

import com.github.DaiYuANg.audit.entity.SysOperationLog;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OperationLogRepository extends BasePanacheCommandRepository<SysOperationLog> {
    @Override
    protected ResultCode notFoundCode() {
        return ResultCode.INTERNAL_ERROR;
    }
}
