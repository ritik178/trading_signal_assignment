package com.zuvomo.signal.dto;

import com.zuvomo.signal.enums.Direction;
import com.zuvomo.signal.enums.SignalStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SignalResponseDTO {

    private String id;
    private String symbol;
    private Direction direction;

    private BigDecimal entryPrice;
    private BigDecimal stopLoss;
    private BigDecimal targetPrice;

    private LocalDateTime entryTime;
    private LocalDateTime expiryTime;
    private LocalDateTime createdAt;

    private SignalStatus status;

    // Live price fetched from Binance at response time
    private BigDecimal currentPrice;

    // ROI calculated live (2 decimal precision)
    private BigDecimal currentRoi;

    // Only populated once signal hits a final state
    private BigDecimal realizedRoi;
}
