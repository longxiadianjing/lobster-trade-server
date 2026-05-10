package com.lobster.trade.service;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * SSE push service for IM real-time messaging.
 * Manages SSE emitter registry per IM session and dispatches events.
 */
public interface ImPushService {

    /**
     * Subscribe current SSE connection to an IM session.
     * @param sessionId the IM session ID
     * @param userId the user ID (for validation)
     * @param token auth token (optional, for reconnect)
     * @return ResponseBodyEmitter for the SSE connection
     */
    ResponseBodyEmitter subscribe(Long sessionId, Long userId, String token);

    /**
     * Push an event to all clients subscribed to a specific IM session.
     * @param sessionId the IM session ID
     * @param eventType event name: "new_message", "session_updated"
     * @param data event payload object
     */
    void pushToSession(Long sessionId, String eventType, Object data);

    /**
     * Unsubscribe an emitter from all sessions.
     * @param emitterToken the token for this subscription
     */
    void unsubscribe(String emitterToken);
}