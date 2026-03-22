package com.github.DaiYuANg.audit.repository;

import com.github.DaiYuANg.audit.entity.SysLoginLog;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LoginLogRepository extends BasePanacheCommandRepository<SysLoginLog> {
    @Override
    protected ResultCode notFoundCode() {
        return ResultCode.INTERNAL_ERROR;
    }
}
