package com.liangdian.audit.repository;

import com.liangdian.audit.entity.SysOperationLog;
import com.liangdian.common.constant.ResultCode;
import com.liangdian.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OperationLogRepository extends BasePanacheCommandRepository<SysOperationLog> {
    @Override
    protected ResultCode notFoundCode() {
        return ResultCode.INTERNAL_ERROR;
    }
}
