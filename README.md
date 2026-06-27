
## Trading Signal Tracker

A Spring Boot backend that lets you create crypto trading signals (BUY/SELL) and automatically tracks them using live prices from Binance.

Built for Zuvomo Backend Skill Evaluation.

---

## What it does

- You create a signal with an entry price, stop loss, target price, and expiry time
- The app fetches live prices from Binance and checks if the signal hit its target or stop loss
- Status updates automatically — either when you call the API or via a background job that runs every 60 seconds
- Once a signal hits TARGET_HIT, STOPLOSS_HIT, or EXPIRED, it stays that way forever

---

---

## How the status logic works

Every time you fetch a signal, the app:

1. Checks if it's already in a final state → if yes, skip everything
2. Checks if current time is past expiry → mark as EXPIRED
3. Checks live Binance price against target/stop loss:
    - BUY: price >= target → TARGET_HIT, price <= stop loss → STOPLOSS_HIT
    - SELL: price <= target → TARGET_HIT, price >= stop loss → STOPLOSS_HIT
4. If none of the above → stays OPEN, returns live ROI

---

## Setup

**Requirements:** Java 17, Maven, MySQL 8

**1. Create the database**

CREATE DATABASE trading_signal_db;

**2. Update your password in application.properties**

spring.datasource.url=jdbc:mysql://localhost:3306/trading_signal_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password_here

**3. Run**

mvn spring-boot:run

Flyway will create the table automatically on first boot. No manual SQL needed.

---

## API Endpoints

| Method | Endpoint | What it does |
|--------|----------|--------------|
| POST | /api/signals | Create a new signal |
| GET | /api/signals | Get all signals with live prices |
| GET | /api/signals/{id} | Get one signal, re-evaluates on the fly |
| DELETE | /api/signals/{id} | Delete a signal |

Swagger UI: http://localhost:8080/swagger-ui.html

---

## Sample Request

POST /api/signals
{
"symbol": "BTCUSDT",
"direction": "BUY",
"entryPrice": 50000,
"stopLoss": 48000,
"targetPrice": 55000,
"entryTime": "2026-06-27T09:00:00",
"expiryTime": "2026-06-28T09:00:00"
}

**Validation rules:**
- BUY: stopLoss < entryPrice < targetPrice
- SELL: targetPrice < entryPrice < stopLoss
- Entry time can be up to 24 hours in the past
- Expiry must be in the future and after entry time

---

## Run with Docker

docker-compose up --build

This starts both MySQL and the app together. No extra setup needed.

---

## Tests

mvn test

Tests cover all status transitions, ROI calculations, BUY/SELL validation rules, and time validation.

```bash
mvn test
```

Tests cover:
- BUY/SELL validation rules
- Time validation
- All status transitions (TARGET_HIT, STOPLOSS_HIT, EXPIRED)
- Final state guard (no re-transition)
- ROI calculation for both directions
