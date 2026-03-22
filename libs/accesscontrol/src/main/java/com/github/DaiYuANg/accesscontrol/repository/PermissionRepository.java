package com.github.DaiYuANg.accesscontrol.repository;

import com.github.DaiYuANg.accesscontrol.entity.SysPermission;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class PermissionRepository extends BasePanacheCommandRepository<SysPermission> {
    public Optional<SysPermission> findByCode(String code) { return find("code", code).firstResultOptional(); }
    public Optional<SysPermission> findByName(String name) { return find("name", name).firstResultOptional(); }
    public long countByCode(String code) { return count("code", code); }

    @Override
    protected ResultCode notFoundCode() {
        return ResultCode.DATA_NOT_FOUND;
    }
}
