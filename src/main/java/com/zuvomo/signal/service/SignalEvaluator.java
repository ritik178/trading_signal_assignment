package com.zuvomo.signal.service;

import com.zuvomo.signal.entity.TradingSignal;
import com.zuvomo.signal.enums.Direction;
import com.zuvomo.signal.enums.SignalStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;


@Component
public class SignalEvaluator {


    public SignalStatus evaluate(TradingSignal signal, BigDecimal livePrice) {
        if (signal.getStatus().isFinal()) {
            return signal.getStatus();
        }

        if (LocalDateTime.now().isAfter(signal.getExpiryTime())) {
            signal.setStatus(SignalStatus.EXPIRED);
            // ROI at expiry moment
            signal.setRealizedRoi(calculateRoi(signal.getDirection(), signal.getEntryPrice(), livePrice));
            return SignalStatus.EXPIRED;
        }

        if (signal.getDirection() == Direction.BUY) {
            if (livePrice.compareTo(signal.getTargetPrice()) >= 0) {
                return markFinal(signal, SignalStatus.TARGET_HIT, livePrice);
            }
            if (livePrice.compareTo(signal.getStopLoss()) <= 0) {
                return markFinal(signal, SignalStatus.STOPLOSS_HIT, livePrice);
            }
        } else { // SELL
            if (livePrice.compareTo(signal.getTargetPrice()) <= 0) {
                return markFinal(signal, SignalStatus.TARGET_HIT, livePrice);
            }
            if (livePrice.compareTo(signal.getStopLoss()) >= 0) {
                return markFinal(signal, SignalStatus.STOPLOSS_HIT, livePrice);
            }
        }

        return SignalStatus.OPEN;
    }


    public BigDecimal calculateRoi(Direction direction, BigDecimal entryPrice, BigDecimal currentPrice) {
        BigDecimal diff = direction == Direction.BUY
                ? currentPrice.subtract(entryPrice)
                : entryPrice.subtract(currentPrice);

        return diff
                .divide(entryPrice, 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }


    private SignalStatus markFinal(TradingSignal signal, SignalStatus newStatus, BigDecimal livePrice) {
        signal.setStatus(newStatus);
        signal.setRealizedRoi(calculateRoi(signal.getDirection(), signal.getEntryPrice(), livePrice));
        return newStatus;
    }
}
