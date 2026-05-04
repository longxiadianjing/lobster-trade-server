package com.lobster.trade.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lobster.trade.common.Result;
import com.lobster.trade.mapper.UserMapper;
import com.lobster.trade.mapper.UserRealNameMapper;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.model.entity.UserRealName;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/real-name")
@RequiredArgsConstructor
public class RealNameAdminController {

    private final UserRealNameMapper realNameMapper;
    private final UserMapper userMapper;

    @GetMapping("/list")
    public Result<IPage<UserRealName>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRealName>();
        if (status != null && !status.isEmpty()) q.eq(UserRealName::getStatus, Integer.parseInt(status));
        q.orderByDesc(UserRealName::getCreateTime);
        var p = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserRealName>(page, size);
        return Result.success(realNameMapper.selectPage(p, q));
    }

    @GetMapping("/detail/{userId}")
    public Result<Map<String, Object>> detail(@PathVariable Long userId) {
        UserRealName r = realNameMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRealName>()
                        .eq(UserRealName::getUserId, userId)
                        .orderByDesc(UserRealName::getCreateTime)
                        .last("LIMIT 1")
        );
        if (r == null) return Result.success(new HashMap<>());

        Map<String, Object> result = new HashMap<>();
        result.put("id", r.getId());
        result.put("userId", r.getUserId());
        result.put("realName", r.getRealName());
        result.put("idCard", r.getIdCard());
        result.put("idCardFront", r.getIdCardFront());
        result.put("idCardBack", r.getIdCardBack());
        result.put("status", r.getStatus());
        result.put("rejectReason", r.getRejectReason());
        result.put("createTime", r.getCreateTime());
        result.put("verifyTime", r.getVerifyTime());
        result.put("aliyunResult", r.getAliyunVerifyResult());

        User user = userMapper.selectById(userId);
        if (user != null) {
            result.put("nickname", user.getNickname());
            result.put("phone", user.getPhone());
        }
        return Result.success(result);
    }

    @PostMapping("/approve/{userId}")
    @Transactional
    public Result<Void> approve(@PathVariable Long userId,
                                 @RequestParam(required = false) String realName,
                                 @RequestParam(required = false) String idCard) {
        UserRealName r = realNameMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRealName>()
                        .eq(UserRealName::getUserId, userId)
                        .in(UserRealName::getStatus, 0, 2)
                        .orderByDesc(UserRealName::getCreateTime)
                        .last("LIMIT 1")
        );
        if (r == null) return Result.error("记录不存在");

        r.setStatus(1);
        r.setVerifyTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        if (realName != null) r.setRealName(realName);
        if (idCard != null) r.setIdCard(idCard);
        realNameMapper.updateById(r);

        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setRealNameStatus(1);
            if (realName != null) user.setRealName(realName);
            if (idCard != null) user.setIdCard(idCard);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
        return Result.success(null);
    }

    @PostMapping("/reject/{userId}")
    @Transactional
    public Result<Void> reject(@PathVariable Long userId, @RequestParam String reason) {
        UserRealName r = realNameMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRealName>()
                        .eq(UserRealName::getUserId, userId)
                        .in(UserRealName::getStatus, 0, 2)
                        .orderByDesc(UserRealName::getCreateTime)
                        .last("LIMIT 1")
        );
        if (r == null) return Result.error("记录不存在");

        r.setStatus(2);
        r.setRejectReason(reason);
        r.setVerifyTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        realNameMapper.updateById(r);

        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setRealNameStatus(2);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
        return Result.success(null);
    }
}