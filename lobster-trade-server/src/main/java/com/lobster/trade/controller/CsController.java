package com.lobster.trade.controller;

import com.lobster.trade.common.Result;
import com.lobster.trade.model.request.CsMessageRequest;
import com.lobster.trade.model.request.CsStartRequest;
import com.lobster.trade.model.response.CsSessionVO;
import com.lobster.trade.service.CsService;
import com.lobster.trade.service.CsPushService;
import com.lobster.trade.service.JwtAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Customer Service (CS) - user-facing endpoints
 */
@RestController
@RequestMapping("/api/cs")
@RequiredArgsConstructor
public class CsController {

    private final CsService csService;
    private final JwtAuthService jwtAuthService;
    private final CsPushService csPushService;

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
        Result<CsSessionVO> result = Result.success(csService.sendMessage(userId, req));
        // Push new message to all subscribers (buyer + admin)
        csPushService.pushToSession(req.getSessionId(), "new_message", result.getData());
        csPushService.broadcastToAdmin("new_message", result.getData());
        return result;
    }

    @PostMapping("/close/{id}")
    public Result<Void> closeSession(@PathVariable Long id,
                                     @RequestHeader("Authorization") String token) {
        Long userId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        csService.closeSession(userId, id);
        csPushService.pushToSession(id, "session_closed", Map.of("sessionId", id));
        csPushService.broadcastToAdmin("session_closed", Map.of("sessionId", id));
        return Result.success(null);
    }

    @GetMapping("/my-sessions")
    public Result<List<CsSessionVO>> getMySessions(@RequestHeader("Authorization") String token) {
        Long userId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        return Result.success(csService.getMySessions(userId));
    }

    /**
     * SSE subscribe to a CS session for real-time events.
     * GET /api/cs/subscribe/{sessionId}?token=xxx
     * Produces text/event-stream for SSE client (EventSource).
     */
    @GetMapping(value = "/subscribe/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public ResponseBodyEmitter subscribe(@PathVariable Long sessionId,
                                        @RequestParam String token,
                                        HttpServletRequest request) {
        Long userId = jwtAuthService.getUserIdFromToken(token.replace("Bearer ", ""));
        // Validate user has access to this session
        csService.getSession(userId, sessionId);
        // Register emitter and get token
        String emitterToken = csPushService.subscribe(sessionId, false, token);
        // Return the emitter - Spring keeps it open and sends events via CsPushService
        return csPushService.getEmitter(emitterToken);
    }
}
