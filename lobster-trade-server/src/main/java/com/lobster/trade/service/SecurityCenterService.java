package com.lobster.trade.service;

import com.lobster.trade.model.entity.UserLoginDevice;
import java.util.List;

public interface SecurityCenterService {

    /** 获取当前用户的可信设备列表 */
    List<UserLoginDevice> getMyDevices(Long userId);

    /** 标记可信/取消可信设备 */
    void toggleTrustDevice(Long userId, Long deviceId);

    /** 移除设备记录 */
    void removeDevice(Long userId, Long deviceId);

    /** 获取账户安全评分（0-100） */
    int getSecurityScore(Long userId);

    /** 获取安全动态（最近的登录/风险事件） */
    List<String> getSecurityEvents(Long userId);

    /** 记录设备登录（用户每次登录时调用） */
    void recordDeviceLogin(Long userId, String deviceFingerprint, String deviceName, String ipAddress);
}
