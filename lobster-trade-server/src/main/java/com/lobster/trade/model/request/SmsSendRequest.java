package com.lobster.trade.model.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class SmsSendRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 验证码类型：register / login / reset_password */
    @NotBlank(message = "类型不能为空")
    private String type;
}
