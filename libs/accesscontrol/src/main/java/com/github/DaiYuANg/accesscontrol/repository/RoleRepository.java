package com.github.DaiYuANg.accesscontrol.repository;

import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class RoleRepository extends BasePanacheCommandRepository<SysRole> {
    public Optional<SysRole> findByName(String name) { return find("name", name).firstResultOptional(); }
    public Optional<SysRole> findByCode(String code) { return find("code", code).firstResultOptional(); }
    public long countByCode(String code) { return count("code", code); }

    @Override
    protected ResultCode notFoundCode() {
        return ResultCode.DATA_NOT_FOUND;
    }
}
