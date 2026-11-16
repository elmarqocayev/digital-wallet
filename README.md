# Digital Wallet & Money Transfer REST API

REST API for wallets and money transfers, built with **Java 21** and **Spring Boot 3.3.5**. The focus is on safe money movement: atomic transfers, deadlock prevention and failed-transfer history.

## Features
- Create a user together with a wallet (AZN, USD or EUR), optional initial balance
- Deposit money, view a wallet, view transaction history
- Atomic transfers between wallets (all-or-nothing)
- Failed transfers are saved in history with status `FAILED`
- Bean Validation, custom exceptions and a global exception handler

## Tech Stack
Java 21 · Spring Boot 3.3.5 · Spring Data JPA · Bean Validation · Lombok · H2 / PostgreSQL · Maven

## Run locally
```bash
mvn spring-boot:run                                        # H2 in-memory database
mvn spring-boot:run -Dspring-boot.run.profiles=postgres    # PostgreSQL (db: walletdb)
```
H2 console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:walletdb`, user: `sa`, empty password)

## API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/users` | Create a user with a wallet |
| GET  | `/api/v1/wallets/{walletId}` | Get a wallet |
| POST | `/api/v1/wallets/deposit` | Deposit money |
| GET  | `/api/v1/wallets/{walletId}/transactions` | Transaction history |
| POST | `/api/v1/transfers` | Transfer money between wallets |

## Example
```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{"senderWalletId":1,"receiverWalletId":2,"amount":25.50}'
```

## Error handling
| Case | HTTP status |
|------|-------------|
| Wallet not found | 404 |
| Email already exists | 409 |
| Insufficient balance, currency mismatch, invalid transfer | 422 |
| Validation errors, malformed JSON | 400 |

## Design notes
- **Atomicity:** `transfer()` is `@Transactional`, so any failure rolls back both balance changes.
- **Concurrency:** both wallets are locked with a pessimistic write lock (`SELECT ... FOR UPDATE`).
- **Deadlock prevention:** wallets are always locked in ascending id order, so simultaneous A→B and B→A transfers cannot wait on each other.
- **Failed transfers survive rollback:** `TransactionRecorder` saves `FAILED` records with `REQUIRES_NEW`. It is a separate bean because Spring's `@Transactional` works through a proxy, and a call inside the same class would bypass it.
- **Money:** all amounts use `BigDecimal` (max 2 decimal places), compared with `compareTo`.
