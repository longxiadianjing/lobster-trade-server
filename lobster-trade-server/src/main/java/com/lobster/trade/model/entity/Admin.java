package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin")
public class Admin {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private String role;       // SUPER_ADMIN / ADMIN / OPERATOR
    private String permissions; // JSON数组，如 ["USER_VIEW","USER_EDIT"] 或 "*" 表示全部权限
    private Integer status;     // 1-正常 2-禁用
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}