Digital Wallet & Money Transfer REST API
Java 21 + Spring Boot 3.3.5 + Spring Data JPA + H2 (PostgreSQL ready).
Run (H2, default)
    mvn spring-boot:run

Run with PostgreSQL
Create database `walletdb`, then:
    mvn spring-boot:run -Dspring-boot.run.profiles=postgres

Endpoints
Method	Endpoint	Purpose	Success
POST	/api/v1/users	Create user and wallet	201
GET	/api/v1/wallets/{id}	View wallet	200
POST	/api/v1/wallets/deposit	Deposit	200
POST	/api/v1/transfers	Transfer	201
GET	/api/v1/wallets/{id}/transactions	History	200
Quick test
    curl -X POST localhost:8080/api/v1/users -H "Content-Type: application/json" \
      -d '{"fullName":"Aysel Mammadova","email":"aysel@mail.com","currency":"AZN","initialBalance":100.00}'

    curl -X POST localhost:8080/api/v1/users -H "Content-Type: application/json" \
      -d '{"fullName":"Kamran Aliyev","email":"kamran@mail.com","currency":"AZN"}'

    curl -X POST localhost:8080/api/v1/wallets/deposit -H "Content-Type: application/json" \
      -d '{"walletId":2,"amount":50.00}'

    curl -X POST localhost:8080/api/v1/transfers -H "Content-Type: application/json" \
      -d '{"senderWalletId":1,"receiverWalletId":2,"amount":30.00}'

    curl localhost:8080/api/v1/wallets/1/transactions
