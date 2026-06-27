package com.zuvomo.signal;

import com.zuvomo.signal.dto.SignalRequestDTO;
import com.zuvomo.signal.enums.Direction;
import com.zuvomo.signal.exception.ValidationException;
import com.zuvomo.signal.service.SignalValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SignalValidator — BUY/SELL rules and time validation")
class SignalValidatorTest {

    private SignalValidator validator;

    @BeforeEach
    void setup() {
        validator = new SignalValidator();
    }

    private SignalRequestDTO validBuy() {
        SignalRequestDTO dto = new SignalRequestDTO();
        dto.setSymbol("BTCUSDT");
        dto.setDirection(Direction.BUY);
        dto.setEntryPrice(new BigDecimal("50000"));
        dto.setStopLoss(new BigDecimal("48000"));   // < entry ✓
        dto.setTargetPrice(new BigDecimal("55000")); // > entry ✓
        dto.setEntryTime(LocalDateTime.now().minusMinutes(30));
        dto.setExpiryTime(LocalDateTime.now().plusHours(23));
        return dto;
    }

    private SignalRequestDTO validSell() {
        SignalRequestDTO dto = new SignalRequestDTO();
        dto.setSymbol("ETHUSDT");
        dto.setDirection(Direction.SELL);
        dto.setEntryPrice(new BigDecimal("3000"));
        dto.setStopLoss(new BigDecimal("3200"));    // > entry ✓
        dto.setTargetPrice(new BigDecimal("2700")); // < entry ✓
        dto.setEntryTime(LocalDateTime.now().minusMinutes(30));
        dto.setExpiryTime(LocalDateTime.now().plusHours(23));
        return dto;
    }

    // ── Valid cases ────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid BUY signal passes validation")
    void validBuySignal() {
        assertThatNoException().isThrownBy(() -> validator.validate(validBuy()));
    }

    @Test
    @DisplayName("Valid SELL signal passes validation")
    void validSellSignal() {
        assertThatNoException().isThrownBy(() -> validator.validate(validSell()));
    }

    // ── BUY price rule failures ────────────────────────────────────────

    @Test
    @DisplayName("BUY: stopLoss >= entryPrice → error")
    void buyStopLossAboveEntry() {
        SignalRequestDTO dto = validBuy();
        dto.setStopLoss(new BigDecimal("51000")); // wrong
        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Validation failed");
    }

    @Test
    @DisplayName("BUY: targetPrice <= entryPrice → error")
    void buyTargetBelowEntry() {
        SignalRequestDTO dto = validBuy();
        dto.setTargetPrice(new BigDecimal("49000")); // wrong
        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(ValidationException.class);
    }

    // ── SELL price rule failures ───────────────────────────────────────

    @Test
    @DisplayName("SELL: stopLoss <= entryPrice → error")
    void sellStopLossBelowEntry() {
        SignalRequestDTO dto = validSell();
        dto.setStopLoss(new BigDecimal("2800")); // wrong — must be > entry
        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("SELL: targetPrice >= entryPrice → error")
    void sellTargetAboveEntry() {
        SignalRequestDTO dto = validSell();
        dto.setTargetPrice(new BigDecimal("3100")); // wrong — must be < entry
        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(ValidationException.class);
    }

    // ── Time validation failures ───────────────────────────────────────

    @Test
    @DisplayName("Entry time more than 24 hours in past → error")
    void entryTimeTooOld() {
        SignalRequestDTO dto = validBuy();
        dto.setEntryTime(LocalDateTime.now().minusHours(25));
        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Expiry before entry → error")
    void expiryBeforeEntry() {
        SignalRequestDTO dto = validBuy();
        dto.setExpiryTime(dto.getEntryTime().minusHours(1));
        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Expiry in the past → error")
    void expiryInPast() {
        SignalRequestDTO dto = validBuy();
        dto.setExpiryTime(LocalDateTime.now().minusMinutes(10));
        assertThatThrownBy(() -> validator.validate(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Entry time up to 24 hours ago is allowed")
    void entryTimeExactly24HoursAgo() {
        SignalRequestDTO dto = validBuy();
        dto.setEntryTime(LocalDateTime.now().minusHours(23).minusMinutes(59));
        assertThatNoException().isThrownBy(() -> validator.validate(dto));
    }
}
