package com.sparta.sogonsogon.noti.component;

import com.sparta.sogonsogon.noti.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {
    private final EmitterRepository emitterRepository;

    @Scheduled(fixedDelay = 30000)
    public void checkEmitters() {
        // reconnect logic
        emitterRepository.closeAllEmitters();
    }
}