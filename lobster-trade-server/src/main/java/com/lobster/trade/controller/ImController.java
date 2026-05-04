package com.lobster.trade.controller;

import com.lobster.trade.model.request.ImSendMessageRequest;
import com.lobster.trade.model.response.ApiResponse;
import com.lobster.trade.model.response.ImSessionVO;
import com.lobster.trade.service.ImService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/im")
@RequiredArgsConstructor
public class ImController {

    private final ImService imService;

    /**
     * 获取我的所有IM会话列表
     * GET /api/im/sessions
     */
    @GetMapping("/sessions")
    public ApiResponse<List<ImSessionVO>> getMySessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(imService.getMySessions(userId));
    }

    /**
     * 获取指定会话详情（含最近消息）
     * GET /api/im/session/{id}
     */
    @GetMapping("/session/{id}")
    public ApiResponse<ImSessionVO> getSession(HttpServletRequest request,
                                               @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(imService.getSession(userId, id));
    }

    /**
     * 根据订单ID获取或创建IM会话
     * POST /api/im/session/by-order/{orderId}
     */
    @PostMapping("/session/by-order/{orderId}")
    public ApiResponse<ImSessionVO> getOrCreateSession(HttpServletRequest request,
                                                       @PathVariable Long orderId) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(imService.getOrCreateSession(userId, orderId));
    }

    /**
     * 根据商品ID获取或创建IM会话（不依赖订单，买卖双方直接沟通）
     * POST /api/im/session/by-product/{productId}
     */
    @PostMapping("/session/by-product/{productId}")
    public ApiResponse<ImSessionVO> getOrCreateSessionByProduct(HttpServletRequest request,
                                                               @PathVariable Long productId) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(imService.getOrCreateSessionByProduct(userId, productId));
    }

    /**
     * 发送IM消息
     * POST /api/im/message
     */
    @PostMapping("/message")
    public ApiResponse<ImSessionVO> sendMessage(HttpServletRequest request,
                                                 @RequestBody ImSendMessageRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(imService.sendMessage(userId, req));
    }

    /**
     * 标记会话已读
     * POST /api/im/session/{id}/read
     */
    @PostMapping("/session/{id}/read")
    public ApiResponse<Void> markRead(HttpServletRequest request,
                                       @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        imService.markRead(userId, id);
        return ApiResponse.success(null);
    }
}