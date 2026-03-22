package com.github.DaiYuANg.accesscontrol.repository;

import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class PermissionGroupRepository extends BasePanacheCommandRepository<SysPermissionGroup> {
    public Optional<SysPermissionGroup> findByCode(String code) { return find("code", code).firstResultOptional(); }
    public Optional<SysPermissionGroup> findByName(String name) { return find("name", name).firstResultOptional(); }
    public long countByCode(String code) { return count("code", code); }
    public long countByName(String name) { return count("name", name); }

    @Override
    protected ResultCode notFoundCode() {
        return ResultCode.DATA_NOT_FOUND;
    }
}
