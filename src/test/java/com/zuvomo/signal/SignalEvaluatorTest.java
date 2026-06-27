package com.zuvomo.signal;

import com.zuvomo.signal.entity.TradingSignal;
import com.zuvomo.signal.enums.Direction;
import com.zuvomo.signal.enums.SignalStatus;
import com.zuvomo.signal.service.SignalEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SignalEvaluator — state machine tests")
class SignalEvaluatorTest {

    private SignalEvaluator evaluator;

    @BeforeEach
    void setup() {
        evaluator = new SignalEvaluator();
    }

    // ── Helper to build a base signal ─────────────────────────────────

    private TradingSignal buySignal() {
        return TradingSignal.builder()
                .id("test-uuid")
                .symbol("BTCUSDT")
                .direction(Direction.BUY)
                .entryPrice(new BigDecimal("50000"))
                .stopLoss(new BigDecimal("48000"))
                .targetPrice(new BigDecimal("55000"))
                .entryTime(LocalDateTime.now().minusHours(1))
                .expiryTime(LocalDateTime.now().plusHours(23))
                .status(SignalStatus.OPEN)
                .build();
    }

    private TradingSignal sellSignal() {
        return TradingSignal.builder()
                .id("test-uuid-2")
                .symbol("ETHUSDT")
                .direction(Direction.SELL)
                .entryPrice(new BigDecimal("3000"))
                .stopLoss(new BigDecimal("3200"))
                .targetPrice(new BigDecimal("2700"))
                .entryTime(LocalDateTime.now().minusHours(1))
                .expiryTime(LocalDateTime.now().plusHours(23))
                .status(SignalStatus.OPEN)
                .build();
    }

    // ── BUY: TARGET_HIT ───────────────────────────────────────────────

    @Test
    @DisplayName("BUY: price >= target → TARGET_HIT")
    void buyTargetHit_exactPrice() {
        TradingSignal signal = buySignal();
        SignalStatus result = evaluator.evaluate(signal, new BigDecimal("55000"));

        assertThat(result).isEqualTo(SignalStatus.TARGET_HIT);
        assertThat(signal.getStatus()).isEqualTo(SignalStatus.TARGET_HIT);
        assertThat(signal.getRealizedRoi()).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("BUY: price above target → TARGET_HIT")
    void buyTargetHit_aboveTarget() {
        TradingSignal signal = buySignal();
        evaluator.evaluate(signal, new BigDecimal("60000"));

        assertThat(signal.getStatus()).isEqualTo(SignalStatus.TARGET_HIT);
    }

    // ── BUY: STOPLOSS_HIT ─────────────────────────────────────────────

    @Test
    @DisplayName("BUY: price <= stopLoss → STOPLOSS_HIT")
    void buyStopLossHit() {
        TradingSignal signal = buySignal();
        SignalStatus result = evaluator.evaluate(signal, new BigDecimal("48000"));

        assertThat(result).isEqualTo(SignalStatus.STOPLOSS_HIT);
        assertThat(signal.getRealizedRoi()).isEqualByComparingTo("-4.00");
    }

    // ── BUY: OPEN ─────────────────────────────────────────────────────

    @Test
    @DisplayName("BUY: price between stop and target → OPEN")
    void buyStillOpen() {
        TradingSignal signal = buySignal();
        SignalStatus result = evaluator.evaluate(signal, new BigDecimal("52000"));

        assertThat(result).isEqualTo(SignalStatus.OPEN);
        assertThat(signal.getStatus()).isEqualTo(SignalStatus.OPEN);
        assertThat(signal.getRealizedRoi()).isNull();
    }

    // ── SELL: TARGET_HIT ──────────────────────────────────────────────

    @Test
    @DisplayName("SELL: price <= target → TARGET_HIT")
    void sellTargetHit() {
        TradingSignal signal = sellSignal();
        evaluator.evaluate(signal, new BigDecimal("2700"));

        assertThat(signal.getStatus()).isEqualTo(SignalStatus.TARGET_HIT);
        assertThat(signal.getRealizedRoi()).isEqualByComparingTo("10.00");
    }

    // ── SELL: STOPLOSS_HIT ────────────────────────────────────────────

    @Test
    @DisplayName("SELL: price >= stopLoss → STOPLOSS_HIT")
    void sellStopLossHit() {
        TradingSignal signal = sellSignal();
        evaluator.evaluate(signal, new BigDecimal("3200"));

        assertThat(signal.getStatus()).isEqualTo(SignalStatus.STOPLOSS_HIT);
        assertThat(signal.getRealizedRoi()).isEqualByComparingTo("-6.67");
    }

    // ── SELL: OPEN ────────────────────────────────────────────────────

    @Test
    @DisplayName("SELL: price between target and stop → OPEN")
    void sellStillOpen() {
        TradingSignal signal = sellSignal();
        SignalStatus result = evaluator.evaluate(signal, new BigDecimal("2900"));

        assertThat(result).isEqualTo(SignalStatus.OPEN);
    }

    // ── EXPIRED ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Expired signal: current time past expiry → EXPIRED")
    void signalExpired() {
        TradingSignal signal = buySignal();
        // Set expiry in the past
        signal.setExpiryTime(LocalDateTime.now().minusMinutes(1));

        evaluator.evaluate(signal, new BigDecimal("52000"));

        assertThat(signal.getStatus()).isEqualTo(SignalStatus.EXPIRED);
    }

    // ── Final state guard ─────────────────────────────────────────────

    @Test
    @DisplayName("Final state (TARGET_HIT) must not re-transition")
    void finalStateNoReTransition() {
        TradingSignal signal = buySignal();
        signal.setStatus(SignalStatus.TARGET_HIT);
        signal.setRealizedRoi(new BigDecimal("10.00"));

        // Even if price drops below stop loss, state must not change
        SignalStatus result = evaluator.evaluate(signal, new BigDecimal("47000"));

        assertThat(result).isEqualTo(SignalStatus.TARGET_HIT);
        assertThat(signal.getStatus()).isEqualTo(SignalStatus.TARGET_HIT);
    }

    @Test
    @DisplayName("Final state (EXPIRED) must not re-transition")
    void expiredStateNoReTransition() {
        TradingSignal signal = buySignal();
        signal.setStatus(SignalStatus.EXPIRED);

        SignalStatus result = evaluator.evaluate(signal, new BigDecimal("60000")); // above target

        assertThat(result).isEqualTo(SignalStatus.EXPIRED);
    }

    // ── ROI calculation ───────────────────────────────────────────────

    @Test
    @DisplayName("BUY ROI calculation — 2 decimal precision")
    void buyRoiCalculation() {
        BigDecimal roi = evaluator.calculateRoi(Direction.BUY, new BigDecimal("50000"), new BigDecimal("55000"));
        assertThat(roi).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("BUY ROI — negative when price below entry")
    void buyRoiNegative() {
        BigDecimal roi = evaluator.calculateRoi(Direction.BUY, new BigDecimal("50000"), new BigDecimal("48000"));
        assertThat(roi).isEqualByComparingTo("-4.00");
    }

    @Test
    @DisplayName("SELL ROI calculation — 2 decimal precision")
    void sellRoiCalculation() {
        BigDecimal roi = evaluator.calculateRoi(Direction.SELL, new BigDecimal("3000"), new BigDecimal("2700"));
        assertThat(roi).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("SELL ROI — negative when price above entry")
    void sellRoiNegative() {
        BigDecimal roi = evaluator.calculateRoi(Direction.SELL, new BigDecimal("3000"), new BigDecimal("3200"));
        assertThat(roi).isEqualByComparingTo("-6.67");
    }
}
