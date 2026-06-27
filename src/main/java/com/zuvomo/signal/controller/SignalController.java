package com.zuvomo.signal.controller;

import com.zuvomo.signal.dto.SignalRequestDTO;
import com.zuvomo.signal.dto.SignalResponseDTO;
import com.zuvomo.signal.service.SignalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/signals")
@RequiredArgsConstructor
@Tag(name = "Trading Signals", description = "Create and track crypto trading signals using live Binance prices")
public class SignalController {

    private final SignalService signalService;


    @PostMapping
    @Operation(
        summary     = "Create a new trading signal",
        description = "Creates a BUY or SELL signal. Entry time may be up to 24 hours in the past. " +
                      "Immediately evaluates against live Binance price."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Signal created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed — check price rules and time constraints")
    })
    public ResponseEntity<SignalResponseDTO> createSignal(
            @Valid @RequestBody SignalRequestDTO request) {

        SignalResponseDTO response = signalService.createSignal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    @Operation(
        summary     = "Get all trading signals",
        description = "Returns all signals with their current live price and ROI from Binance."
    )
    @ApiResponse(responseCode = "200", description = "List of signals")
    public ResponseEntity<List<SignalResponseDTO>> getAllSignals() {
        return ResponseEntity.ok(signalService.getAllSignals());
    }


    @GetMapping("/{id}")
    @Operation(
        summary     = "Get a signal by ID",
        description = "Fetches live price from Binance and re-evaluates the signal status on every call."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Signal found"),
        @ApiResponse(responseCode = "404", description = "Signal not found")
    })
    public ResponseEntity<SignalResponseDTO> getSignalById(
            @Parameter(description = "UUID of the signal") @PathVariable String id) {

        return ResponseEntity.ok(signalService.getSignalById(id));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a signal by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Signal deleted"),
        @ApiResponse(responseCode = "404", description = "Signal not found")
    })
    public ResponseEntity<Void> deleteSignal(
            @Parameter(description = "UUID of the signal") @PathVariable String id) {

        signalService.deleteSignal(id);
        return ResponseEntity.noContent().build();
    }
}
