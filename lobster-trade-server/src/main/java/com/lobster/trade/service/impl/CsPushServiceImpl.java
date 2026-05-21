package com.lobster.trade.service.impl;

import com.lobster.trade.service.CsPushService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory SSE push service for CS real-time messaging.
 * Manages a registry of active SSE emitters per session and admin.
 */
@Slf4j
@Service
public class CsPushServiceImpl implements CsPushService {

    /**
     * One emitter entry: holds the ResponseBodyEmitter + metadata
     */
    private static class EmitterEntry {
        final ResponseBodyEmitter emitter;
        final Long sessionId;
        final boolean isAdmin;
        final String token;
        final UUID id = UUID.randomUUID();

        EmitterEntry(ResponseBodyEmitter emitter, Long sessionId, boolean isAdmin, String token) {
            this.emitter = emitter;
            this.sessionId = sessionId;
            this.isAdmin = isAdmin;
            this.token = token;
        }
    }

    // Per-session subscriber emitters (for user-side listeners)
    private final Map<Long, List<EmitterEntry>> sessionEmitters = new ConcurrentHashMap<>();
    // Admin broadcast emitters (all admins receive these)
    private final List<EmitterEntry> adminEmitters = new CopyOnWriteArrayList<>();
    // Token -> emitter entry (for cleanup)
    private final Map<String, EmitterEntry> tokenToEntry = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT_MS = 60_000L;

    @Override
    public String subscribe(Long sessionId, boolean isAdmin, String token) {
        // Create emitter with timeout
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(SSE_TIMEOUT_MS) {
            @Override
            public void onTimeout(Runnable callback) {
                super.onTimeout(callback);
            }
        };

        EmitterEntry entry = new EmitterEntry(emitter, sessionId, isAdmin, token);
        String emitterToken = entry.id.toString();
        tokenToEntry.put(emitterToken, entry);

        if (isAdmin) {
            adminEmitters.add(entry);
            log.info("[CS-PUSH] Admin subscribed, token={}", emitterToken);
        } else {
            sessionEmitters.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(entry);
            log.info("[CS-PUSH] User subscribed to session {}, token={}", sessionId, emitterToken);
        }

        // Auto-cleanup when emitter completes
        emitter.onCompletion(() -> {
            removeEntry(emitterToken);
            log.info("[CS-PUSH] Emitter completed, token={}", emitterToken);
        });
        emitter.onError(e -> {
            removeEntry(emitterToken);
            log.warn("[CS-PUSH] Emitter error, token={}", emitterToken, e);
        });
        emitter.onTimeout(() -> {
            removeEntry(emitterToken);
            log.info("[CS-PUSH] Emitter timeout, token={}", emitterToken);
        });

        return emitterToken;
    }

    @Override
    public ResponseBodyEmitter getEmitter(String emitterToken) {
        EmitterEntry entry = tokenToEntry.get(emitterToken);
        return entry != null ? entry.emitter : null;
    }

    private void removeEntry(String emitterToken) {
        EmitterEntry entry = tokenToEntry.remove(emitterToken);
        if (entry != null) {
            if (entry.isAdmin) {
                adminEmitters.remove(entry);
            } else {
                List<EmitterEntry> list = sessionEmitters.get(entry.sessionId);
                if (list != null) {
                    list.remove(entry);
                    if (list.isEmpty()) {
                        sessionEmitters.remove(entry.sessionId);
                    }
                }
            }
        }
    }

    @Override
    public void unsubscribe(String emitterToken) {
        removeEntry(emitterToken);
    }

    @Override
    public void pushToSession(Long sessionId, String eventType, Object data) {
        List<EmitterEntry> entries = sessionEmitters.get(sessionId);
        if (entries == null || entries.isEmpty()) {
            log.debug("[CS-PUSH] No subscribers for session {}", sessionId);
            return;
        }
        String sseData = serializeEvent(eventType, data);
        List<EmitterEntry> dead = new ArrayList<>();
        for (EmitterEntry e : entries) {
            try {
                e.emitter.send(sseData.getBytes());
            } catch (IOException ex) {
                dead.add(e);
                log.warn("[CS-PUSH] Failed to push to emitter, removing", ex);
            }
        }
        // Clean up dead emitters
        for (EmitterEntry e : dead) {
            removeEntry(e.id.toString());
        }
    }

    @Override
    public void broadcastToAdmin(String eventType, Object data) {
        if (adminEmitters.isEmpty()) {
            log.debug("[CS-PUSH] No admin subscribers");
            return;
        }
        String sseData = serializeEvent(eventType, data);
        List<EmitterEntry> dead = new ArrayList<>();
        for (EmitterEntry e : adminEmitters) {
            try {
                e.emitter.send(sseData.getBytes());
            } catch (IOException ex) {
                dead.add(e);
                log.warn("[CS-PUSH] Failed to push to admin emitter, removing", ex);
            }
        }
        for (EmitterEntry e : dead) {
            removeEntry(e.id.toString());
        }
    }

    /**
     * Serialize event as SSE format string.
     */
    private String serializeEvent(String eventType, Object data) {
        return "event: " + eventType + "\ndata: " + JSON.toJSONString(data) + "\n\n";
    }
}
