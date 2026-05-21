package com.lobster.trade.controller;

import com.lobster.trade.model.request.WithdrawRequest;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/info")
    public ApiResponse<com.lobster.trade.model.entity.Wallet> getWalletInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        com.lobster.trade.model.entity.Wallet wallet = walletService.getWalletInfo(userId);
        return ApiResponse.success(wallet);
    }

    @PostMapping("/recharge")
    public ApiResponse<Void> recharge(HttpServletRequest request,
                                      @Validated @RequestBody com.lobster.trade.model.request.RechargeRequest rechargeRequest) {
        Long userId = (Long) request.getAttribute("userId");
        walletService.recharge(userId, rechargeRequest);
        return ApiResponse.success("充值请求成功");
    }

    @PostMapping("/withdraw")
    public ApiResponse<Void> withdraw(HttpServletRequest request,
                                       @Validated @RequestBody WithdrawRequest withdrawRequest) {
        Long userId = (Long) request.getAttribute("userId");
        walletService.withdraw(userId, withdrawRequest);
        return ApiResponse.success("提现申请已提交");
    }

    @GetMapping("/transactions")
    public ApiResponse<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.lobster.trade.model.entity.WalletTransaction>> getTransactions(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(walletService.getTransactions(userId, page, pageSize));
    }
}
