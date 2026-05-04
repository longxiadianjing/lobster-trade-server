package com.lobster.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lobster.trade.model.entity.UserSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSessionMapper extends BaseMapper<UserSession> {
}
