package com.lobster.trade.service;

import com.lobster.trade.model.entity.User;
import com.lobster.trade.controller.UserController.RealNameStatusVO;

public interface UserService {

    /**
     * 获取当前登录用户信息
     */
    User getCurrentUser(Long userId);

    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, User user);

    /**
     * 设置支付密码
     */
    void setPayPassword(Long userId, String payPassword);

    /**
     * 申请实名认证
     */
    void applyRealName(Long userId, String realName, String idCard);

    /**
     * 查询实名状态
     */
    RealNameStatusVO getRealNameStatus(Long userId);
}
