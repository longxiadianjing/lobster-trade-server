package com.lobster.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lobster.trade.model.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {

    @Select("SELECT id, user_id, balance, frozen_balance, total_income, total_expense, " +
            "password_set, password, alipay_account, alipay_name, wechat_openid, " +
            "bank_card_no, bank_name, bank_username, create_time, update_time, is_deleted " +
            "FROM wallet WHERE user_id = #{userId}")
    Wallet selectWalletWithPassword(@Param("userId") Long userId);
}
