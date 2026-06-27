package com.zuvomo.signal.service;

import com.zuvomo.signal.client.BinancePriceClient;
import com.zuvomo.signal.dto.SignalRequestDTO;
import com.zuvomo.signal.dto.SignalResponseDTO;
import com.zuvomo.signal.entity.TradingSignal;
import com.zuvomo.signal.enums.SignalStatus;
import com.zuvomo.signal.exception.SignalNotFoundException;
import com.zuvomo.signal.repository.SignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignalService {

    private final SignalRepository    signalRepository;
    private final SignalValidator     signalValidator;
    private final SignalEvaluator     signalEvaluator;
    private final BinancePriceClient  binanceClient;



    @Transactional
    public SignalResponseDTO createSignal(SignalRequestDTO dto) {
        signalValidator.validate(dto);

        TradingSignal signal = TradingSignal.builder()
                .symbol(dto.getSymbol().toUpperCase())
                .direction(dto.getDirection())
                .entryPrice(dto.getEntryPrice())
                .stopLoss(dto.getStopLoss())
                .targetPrice(dto.getTargetPrice())
                .entryTime(dto.getEntryTime())
                .expiryTime(dto.getExpiryTime())
                .status(SignalStatus.OPEN)
                .build();

        TradingSignal saved = signalRepository.save(signal);
        log.info("Signal created: id={} symbol={} direction={}", saved.getId(), saved.getSymbol(), saved.getDirection());

        BigDecimal livePrice = fetchPrice(saved.getSymbol());
        signalEvaluator.evaluate(saved, livePrice);
        signalRepository.save(saved); // persist if status changed on creation

        return toResponse(saved, livePrice);
    }


    @Transactional(readOnly = true)
    public List<SignalResponseDTO> getAllSignals() {
        return signalRepository.findAll().stream()
                .map(signal -> {
                    BigDecimal livePrice = fetchPrice(signal.getSymbol());
                    return toResponse(signal, livePrice);
                })
                .toList();
    }


    @Transactional
    public SignalResponseDTO getSignalById(String id) {
        TradingSignal signal = findOrThrow(id);

        BigDecimal livePrice = fetchPrice(signal.getSymbol());

        signalEvaluator.evaluate(signal, livePrice);
        signalRepository.save(signal);

        return toResponse(signal, livePrice);
    }


    @Transactional
    public void deleteSignal(String id) {
        TradingSignal signal = findOrThrow(id);
        signalRepository.delete(signal);
        log.info("Signal deleted: id={}", id);
    }

    @Transactional
    public void evaluateAllOpenSignals() {
        List<TradingSignal> openSignals = signalRepository.findAllByStatus(SignalStatus.OPEN);
        log.info("Scheduler: evaluating {} OPEN signals", openSignals.size());

        for (TradingSignal signal : openSignals) {
            try {
                BigDecimal livePrice = fetchPrice(signal.getSymbol());
                SignalStatus before = signal.getStatus();
                signalEvaluator.evaluate(signal, livePrice);
                signalRepository.save(signal);

                if (signal.getStatus() != before) {
                    log.info("Signal {} transitioned {} → {}", signal.getId(), before, signal.getStatus());
                }
            } catch (Exception e) {
                // Don't let one bad price fetch fail the entire batch
                log.error("Error evaluating signal {}: {}", signal.getId(), e.getMessage());
            }
        }
    }


    private TradingSignal findOrThrow(String id) {
        return signalRepository.findById(id)
                .orElseThrow(() -> new SignalNotFoundException("Signal not found with id: " + id));
    }

    private BigDecimal fetchPrice(String symbol) {
        try {
            return binanceClient.getPrice(symbol);
        } catch (Exception e) {
            log.warn("Could not fetch Binance price for {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Unable to fetch live price for symbol: " + symbol + ". Please try again.");
        }
    }

    private SignalResponseDTO toResponse(TradingSignal signal, BigDecimal livePrice) {
        BigDecimal currentRoi = signal.getStatus() == SignalStatus.OPEN
                ? signalEvaluator.calculateRoi(signal.getDirection(), signal.getEntryPrice(), livePrice)
                : null;

        return SignalResponseDTO.builder()
                .id(signal.getId())
                .symbol(signal.getSymbol())
                .direction(signal.getDirection())
                .entryPrice(signal.getEntryPrice())
                .stopLoss(signal.getStopLoss())
                .targetPrice(signal.getTargetPrice())
                .entryTime(signal.getEntryTime())
                .expiryTime(signal.getExpiryTime())
                .createdAt(signal.getCreatedAt())
                .status(signal.getStatus())
                .currentPrice(livePrice)
                .currentRoi(currentRoi)
                .realizedRoi(signal.getRealizedRoi())
                .build();
    }
}
