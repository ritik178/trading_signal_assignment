package com.zuvomo.signal.entity;

import com.zuvomo.signal.enums.Direction;
import com.zuvomo.signal.enums.SignalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trading_signal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradingSignal {

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private Direction direction;

    @Column(name = "entry_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal entryPrice;

    @Column(name = "stop_loss", nullable = false, precision = 20, scale = 8)
    private BigDecimal stopLoss;

    @Column(name = "target_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal targetPrice;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SignalStatus status = SignalStatus.OPEN;

    @Column(name = "realized_roi", precision = 10, scale = 2)
    private BigDecimal realizedRoi;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = SignalStatus.OPEN;
        }
    }
}
