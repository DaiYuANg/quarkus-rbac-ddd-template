package com.liangdian.accesscontrol.repository;

import com.liangdian.accesscontrol.entity.SysPermissionGroup;
import com.liangdian.common.constant.ResultCode;
import com.liangdian.persistence.repository.BasePanacheCommandRepository;
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
