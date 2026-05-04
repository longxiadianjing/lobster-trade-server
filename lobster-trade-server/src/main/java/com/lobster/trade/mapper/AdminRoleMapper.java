package com.lobster.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lobster.trade.model.entity.AdminRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminRoleMapper extends BaseMapper<AdminRole> {

    @Select("SELECT * FROM admin_role WHERE role_code = #{roleCode} AND is_deleted = 0 LIMIT 1")
    AdminRole selectByRoleCode(String roleCode);
}