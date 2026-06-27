package com.zuvomo.signal.scheduler;

import com.zuvomo.signal.service.SignalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SignalScheduler {

    private final SignalService signalService;

    @Scheduled(fixedDelayString = "${signal.scheduler.interval-ms}")
    public void evaluateOpenSignals() {
        log.info("Scheduler triggered — evaluating all OPEN signals");
        try {
            signalService.evaluateAllOpenSignals();
        } catch (Exception e) {
            log.error("Scheduler failed: {}", e.getMessage());
        }
    }
}
