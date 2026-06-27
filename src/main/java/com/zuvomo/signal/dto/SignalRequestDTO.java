package com.zuvomo.signal.dto;

import com.zuvomo.signal.enums.Direction;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SignalRequestDTO {

    @NotBlank(message = "Symbol must not be blank (e.g. BTCUSDT)")
    @Pattern(regexp = "^[A-Z]{2,20}$", message = "Symbol must be uppercase letters only (e.g. BTCUSDT)")
    private String symbol;

    @NotNull(message = "Direction must be BUY or SELL")
    private Direction direction;

    @NotNull(message = "Entry price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Entry price must be greater than 0")
    private BigDecimal entryPrice;

    @NotNull(message = "Stop loss is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Stop loss must be greater than 0")
    private BigDecimal stopLoss;

    @NotNull(message = "Target price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Target price must be greater than 0")
    private BigDecimal targetPrice;

    @NotNull(message = "Entry time is required")
    private LocalDateTime entryTime;

    @NotNull(message = "Expiry time is required")
    private LocalDateTime expiryTime;
}
