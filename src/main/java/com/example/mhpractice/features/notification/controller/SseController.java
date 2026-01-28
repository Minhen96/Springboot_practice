package com.example.mhpractice.features.notification.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.mhpractice.features.notification.service.SseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    // TODO: In production, extract userId from SecurityContext/JWT credential
    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String userId) {
        return sseService.subscribe(userId);
    }
}
