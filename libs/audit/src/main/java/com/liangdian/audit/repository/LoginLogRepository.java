package com.liangdian.audit.repository;

import com.liangdian.audit.entity.SysLoginLog;
import com.liangdian.common.constant.ResultCode;
import com.liangdian.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LoginLogRepository extends BasePanacheCommandRepository<SysLoginLog> {
    @Override
    protected ResultCode notFoundCode() {
        return ResultCode.INTERNAL_ERROR;
    }
}
