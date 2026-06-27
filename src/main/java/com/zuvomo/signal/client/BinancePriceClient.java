package com.zuvomo.signal.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class BinancePriceClient {

    private final WebClient webClient;

    public BinancePriceClient(@Value("${binance.api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }


    @SuppressWarnings("unchecked")
    public BigDecimal getPrice(String symbol) {
        Map<String, String> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v3/ticker/price")
                        .queryParam("symbol", symbol.toUpperCase())
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block(); // blocking is fine — we're in a standard MVC thread

        if (response == null || !response.containsKey("price")) {
            throw new RuntimeException("Could not fetch price for symbol: " + symbol);
        }

        return new BigDecimal(response.get("price"));
    }
}
