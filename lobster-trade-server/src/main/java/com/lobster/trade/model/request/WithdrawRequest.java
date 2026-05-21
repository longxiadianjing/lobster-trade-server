package com.lobster.trade.model.request;

import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class WithdrawRequest {

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "提现渠道不能为空")
    private String channel;

    /** 账户信息（银行卡号/支付宝账号/微信号） */
    @NotBlank(message = "账户信息不能为空")
    private String accountInfo;

    @NotBlank(message = "支付密码不能为空")
    private String payPassword;
}
