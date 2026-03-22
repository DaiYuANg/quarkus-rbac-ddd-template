package com.liangdian.identity.repository;

import com.liangdian.common.constant.ResultCode;
import com.liangdian.identity.entity.SysUser;
import com.liangdian.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UserRepository extends BasePanacheCommandRepository<SysUser> {
    public Optional<SysUser> findByUsername(String username) { return find("username", username).firstResultOptional(); }
    public long countByUsername(String username) { return count("username", username); }
    public long countByEmail(String email) { return count("email", email); }
    public long countByMobilePhone(String mobilePhone) { return count("mobilePhone", mobilePhone); }
    public long countByIdentifier(String identifier) { return count("identifier", identifier); }

    @Override
    protected ResultCode notFoundCode() {
        return ResultCode.DATA_NOT_FOUND;
    }
}
