package com.lobster.trade.service;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * SSE push service for CS (customer service) real-time messaging.
 * Manages SSE emitter registry and dispatches events to subscribed clients.
 */
public interface CsPushService {

    /**
     * Push an event to all clients subscribed to a specific CS session.
     * @param sessionId the CS session ID
     * @param eventType event name: "new_message", "session_closed", "operator_assigned"
     * @param data event payload object
     */
    void pushToSession(Long sessionId, String eventType, Object data);

    /**
     * Broadcast an event to all admin subscribers.
     * @param eventType event name
     * @param data event payload
     */
    void broadcastToAdmin(String eventType, Object data);

    /**
     * Subscribe current thread (SSE emitter) to a CS session.
     * @param sessionId the session ID
     * @param isAdmin true if subscriber is admin
     * @param token user/admin token (for auth on reconnect)
     * @return emitter token for this subscription
     */
    String subscribe(Long sessionId, boolean isAdmin, String token);

    /**
     * Get the ResponseBodyEmitter for a given token.
     * Used by the SSE controller endpoint to return the emitter.
     * @param emitterToken token returned from subscribe()
     * @return the ResponseBodyEmitter
     */
    ResponseBodyEmitter getEmitter(String emitterToken);

    /**
     * Unsubscribe an emitter from all sessions.
     * @param emitterToken the token returned from subscribe()
     */
    void unsubscribe(String emitterToken);
}
