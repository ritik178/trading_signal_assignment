package com.zuvomo.signal.service;

import com.zuvomo.signal.dto.SignalRequestDTO;
import com.zuvomo.signal.enums.Direction;
import com.zuvomo.signal.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Component
public class SignalValidator {

    public void validate(SignalRequestDTO dto) {
        List<String> errors = new ArrayList<>();

        validatePriceRules(dto, errors);
        validateTimeRules(dto, errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }


    private void validatePriceRules(SignalRequestDTO dto, List<String> errors) {
        if (dto.getDirection() == null || dto.getEntryPrice() == null
                || dto.getStopLoss() == null || dto.getTargetPrice() == null) {
            return;
        }

        if (dto.getDirection() == Direction.BUY) {
            // BUY: stopLoss < entryPrice < targetPrice
            if (dto.getStopLoss().compareTo(dto.getEntryPrice()) >= 0) {
                errors.add("BUY signal: stop_loss must be less than entry_price");
            }
            if (dto.getTargetPrice().compareTo(dto.getEntryPrice()) <= 0) {
                errors.add("BUY signal: target_price must be greater than entry_price");
            }
        } else { // SELL
            // SELL: targetPrice < entryPrice < stopLoss
            if (dto.getStopLoss().compareTo(dto.getEntryPrice()) <= 0) {
                errors.add("SELL signal: stop_loss must be greater than entry_price");
            }
            if (dto.getTargetPrice().compareTo(dto.getEntryPrice()) >= 0) {
                errors.add("SELL signal: target_price must be less than entry_price");
            }
        }
    }


    private void validateTimeRules(SignalRequestDTO dto, List<String> errors) {
        if (dto.getEntryTime() == null || dto.getExpiryTime() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime earliestAllowed = now.minusHours(24);
        if (dto.getEntryTime().isBefore(earliestAllowed)) {
            errors.add("entry_time cannot be more than 24 hours in the past");
        }
        if (dto.getEntryTime().isAfter(now)) {
            errors.add("entry_time cannot be in the future");
        }


        if (!dto.getExpiryTime().isAfter(dto.getEntryTime())) {
            errors.add("expiry_time must be after entry_time");
        }

        if (!dto.getExpiryTime().isAfter(now)) {
            errors.add("expiry_time must be in the future");
        }
    }
}
