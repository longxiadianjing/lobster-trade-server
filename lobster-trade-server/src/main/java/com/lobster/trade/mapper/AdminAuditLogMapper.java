package com.lobster.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lobster.trade.model.entity.AdminAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminAuditLogMapper extends BaseMapper<AdminAuditLog> {
}
