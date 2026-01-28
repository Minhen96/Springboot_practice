package com.example.mhpractice.features.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SseService {

    // Store active emitters. Key is userId (String).
    // In a real production app with multiple instances, use Redis Pub/Sub.
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String userId) {
        // Timeout: 0 means infinite (or server default).
        // 1 hour = 3600000 ms.
        SseEmitter emitter = new SseEmitter(3600_000L); // 1 hour timeout

        emitter.onCompletion(() -> {
            log.debug("SSE emitter completed for user: {}", userId);
            emitters.remove(userId);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE emitter timed out for user: {}", userId);
            emitter.complete();
            emitters.remove(userId);
        });

        emitter.onError((e) -> {
            log.debug("SSE emitter error for user: {}", userId, e);
            emitter.complete();
            emitters.remove(userId);
        });

        emitters.put(userId, emitter);
        log.info("User subscribed to SSE: {}", userId);

        return emitter;
    }

    public void send(String userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("No active SSE emitter for user: {}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            log.warn("Failed to send SSE to user: {}. Removing emitter.", userId);
            emitters.remove(userId);
        }
    }
}
