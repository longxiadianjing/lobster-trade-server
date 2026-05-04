package com.lobster.trade.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin_role")
public class AdminRole {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleCode;   // 角色代码，如 SUPER_ADMIN / CONTENT_EDITOR
    private String roleName;   // 角色名称，如 超级管理员 / 内容编辑
    private String permissions; // JSON数组，如 ["USER_VIEW","ORDER_VIEW"]
    private String description; // 角色描述
    private Integer status;    // 1-启用 0-禁用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}