package com.lobster.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lobster.trade.model.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}