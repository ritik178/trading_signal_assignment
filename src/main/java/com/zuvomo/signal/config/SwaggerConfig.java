package com.zuvomo.signal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trading Signal Tracking API")
                        .description("Backend for tracking crypto trading signals using live Binance prices. " +
                                     "Signals auto-transition to TARGET_HIT, STOPLOSS_HIT, or EXPIRED.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ritik Singh")
                                .email("ritikstm16@gmail.com")));
    }
}
