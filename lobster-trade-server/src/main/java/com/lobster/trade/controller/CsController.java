package com.lobster.trade.controller;

import com.lobster.trade.common.Result;
import com.lobster.trade.model.request.CsMessageRequest;
import com.lobster.trade.model.request.CsStartRequest;
import com.lobster.trade.model.response.CsSessionVO;
import com.lobster.trade.service.CsService;
import com.lobster.trade.service.JwtAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cs")
@RequiredArgsConstructor
public class CsController {

    private final CsService csService;
    private final JwtAuthService jwtAuthService;

    @PostMapping("/start")
    public Result<CsSessionVO> startSession(@RequestBody CsStartRequest req,
                                           @RequestHeader("Authorization") String token) {
        Long userId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        return Result.success(csService.startSession(userId, req));
    }

    @GetMapping("/session/{id}")
    public Result<CsSessionVO> getSession(@PathVariable Long id,
                                         @RequestHeader("Authorization") String token) {
        Long userId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        return Result.success(csService.getSession(userId, id));
    }

    @PostMapping("/message")
    public Result<CsSessionVO> sendMessage(@RequestBody CsMessageRequest req,
                                           @RequestHeader("Authorization") String token) {
        Long userId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        return Result.success(csService.sendMessage(userId, req));
    }

    @PostMapping("/close/{id}")
    public Result<Void> closeSession(@PathVariable Long id,
                                     @RequestHeader("Authorization") String token) {
        Long userId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        csService.closeSession(userId, id);
        return Result.success(null);
    }

    @GetMapping("/my-sessions")
    public Result<List<CsSessionVO>> getMySessions(@RequestHeader("Authorization") String token) {
        Long userId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        return Result.success(csService.getMySessions(userId));
    }
}