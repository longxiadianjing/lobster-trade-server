package com.lobster.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lobster.trade.mapper.UserLoginDeviceMapper;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.entity.UserLoginDevice;
import com.lobster.trade.service.SecurityCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityCenterServiceImpl implements SecurityCenterService {

    private final UserLoginDeviceMapper deviceMapper;
    private final UserMapper userMapper;

    @Override
    public List<UserLoginDevice> getMyDevices(Long userId) {
        return deviceMapper.selectList(
            new LambdaQueryWrapper<UserLoginDevice>()
                .eq(UserLoginDevice::getUserId, userId)
                .eq(UserLoginDevice::getIsDeleted, 0)
                .orderByDesc(UserLoginDevice::getLastActiveTime)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTrustDevice(Long userId, Long deviceId) {
        UserLoginDevice device = deviceMapper.selectById(deviceId);
        if (device == null || device.getIsDeleted() == 1 || !device.getUserId().equals(userId)) {
            return;
        }
        device.setIsTrusted(device.getIsTrusted() == 1 ? 0 : 1);
        deviceMapper.updateById(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDevice(Long userId, Long deviceId) {
        UserLoginDevice device = deviceMapper.selectById(deviceId);
        if (device != null && device.getUserId().equals(userId)) {
            deviceMapper.deleteById(deviceId);
        }
    }

    @Override
    public int getSecurityScore(Long userId) {
        int score = 100;
        User user = userMapper.selectById(userId);
        if (user == null) return 0;

        if (user.getPayPassword() == null || user.getPayPassword().isEmpty()) score -= 30;
        if (user.getRealNameStatus() == null || user.getRealNameStatus() != 2) score -= 20;
        if (user.getPhone() == null || user.getPhone().isEmpty()) score -= 15;

        long deviceCount = deviceMapper.selectCount(
            new LambdaQueryWrapper<UserLoginDevice>()
                .eq(UserLoginDevice::getUserId, userId)
                .eq(UserLoginDevice::getIsDeleted, 0)
        );
        if (deviceCount <= 1) score -= 15;
        if (deviceCount == 0) score -= 20;

        return Math.max(0, score);
    }

    @Override
    public List<String> getSecurityEvents(Long userId) {
        List<String> events = new ArrayList<>();
        User user = userMapper.selectById(userId);
        if (user == null) return events;

        if (user.getPayPassword() == null || user.getPayPassword().isEmpty()) {
            events.add("⚠️ 未设置支付密码，建议尽快设置");
        }
        if (user.getRealNameStatus() == null || user.getRealNameStatus() != 2) {
            events.add("⚠️ 未完成实名认证，无法发起较高金额交易");
        }

        long loginDays = 0;
        if (user.getLastLoginTime() != null) {
            loginDays = java.time.temporal.ChronoUnit.DAYS.between(user.getLastLoginTime(), LocalDateTime.now());
        }
        if (loginDays > 30) {
            events.add("📅 超过 " + loginDays + " 天未登录，建议检查账户安全");
        }

        long deviceCount = deviceMapper.selectCount(
            new LambdaQueryWrapper<UserLoginDevice>()
                .eq(UserLoginDevice::getUserId, userId)
                .eq(UserLoginDevice::getIsDeleted, 0)
        );
        if (deviceCount >= 3) {
            events.add("📱 当前有 " + deviceCount + " 台登录设备，建议检查并移除不熟悉的设备");
        }

        if (events.isEmpty()) {
            events.add("✅ 账户安全状态良好");
        }
        return events;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordDeviceLogin(Long userId, String deviceFingerprint, String deviceName, String ipAddress) {
        // 检查是否已有该设备记录
        LambdaQueryWrapper<UserLoginDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLoginDevice::getUserId, userId)
               .eq(UserLoginDevice::getDeviceFingerprint, deviceFingerprint)
               .eq(UserLoginDevice::getIsDeleted, 0);
        List<UserLoginDevice> existing = deviceMapper.selectList(wrapper);

        LocalDateTime now = LocalDateTime.now();
        if (!existing.isEmpty()) {
            // 更新已有设备
            UserLoginDevice device = existing.get(0);
            device.setLastActiveTime(now);
            device.setIpAddress(ipAddress);
            device.setDeviceName(deviceName);
            deviceMapper.updateById(device);
        } else {
            // 新增设备记录
            UserLoginDevice device = new UserLoginDevice();
            device.setUserId(userId);
            device.setDeviceFingerprint(deviceFingerprint);
            device.setDeviceName(deviceName);
            device.setIpAddress(ipAddress);
            device.setIsCurrent(0);
            device.setIsTrusted(0);
            device.setFirstLoginTime(now);
            device.setLastActiveTime(now);
            deviceMapper.insert(device);
        }

        // 更新用户最后登录
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setLastLoginTime(now);
            user.setLastLoginIp(ipAddress);
            userMapper.updateById(user);
        }
    }

    @Override
    public List<UserLoginDevice> getLoginHistory(Long userId) {
        return deviceMapper.selectList(
            new LambdaQueryWrapper<UserLoginDevice>()
                .eq(UserLoginDevice::getUserId, userId)
                .eq(UserLoginDevice::getIsDeleted, 0)
                .orderByDesc(UserLoginDevice::getLastActiveTime)
                .last("LIMIT 50")
        );
    }
}
