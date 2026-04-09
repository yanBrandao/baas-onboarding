# Design: baas-account

## Layered Architecture (mirrors baas-onboarding)
```
controller → usecase → repository (interface)
                           ↓
                   infrastructure/repository (DynamoDB impl)
                   infrastructure/config (DynamoConfig)
```

## Idempotency Strategy
- Client sends `Idempotency-Key: <uuid>` header on checkin/checkout
- `transaction_id` IS the idempotency key — DynamoDB PK+SK uniqueness enforces deduplication
- `putItem` on Transaction table with `conditionExpression("attribute_not_exists(account_id)")`:
  - Success → new transaction
  - `ConditionalCheckFailedException` → already exists → query and return existing result

## Concurrency Strategy (OCC)
- `AccountEntity.version` annotated with `@DynamoDbVersionAttribute`
- DynamoDB Enhanced Client adds condition check on every `updateItem` call
- If another writer modified the account between our read and write → `ConditionalCheckFailedException`
- Use-case retries up to 3 times; throws `ConcurrentModificationException` → HTTP 409 after exhaustion

## Checkin / Checkout Flow
```
1. Validate Idempotency-Key present
2. Check existing transaction (by account_id + transaction_id)  → return if found
3. Retry loop (max 3):
   a. Get account (read version)
   b. Validate status ACTIVE, sufficient balance (checkout only)
   c. Compute new balance
   d. putItem Transaction (condition: attribute_not_exists)   ← idempotency gate
   e. updateItem Account with new balance                     ← OCC gate
   f. On ConditionalCheckFailedException from (e) → retry
   g. On ConditionalCheckFailedException from (d) → return existing (race on idempotency)
4. Return transaction
```

## AccountEntity
```java
@DynamoDbBean
class AccountEntity {
  String accountId;     @DynamoDbPartitionKey @DynamoDbAttribute("account_id")
  String onboardingId;
  String currency;
  BigDecimal balance;
  String status;
  Long version;         @DynamoDbVersionAttribute  ← OCC
  String createdAt;
}
```

## TransactionEntity
```java
@DynamoDbBean
class TransactionEntity {
  String accountId;      @DynamoDbPartitionKey @DynamoDbAttribute("account_id")
  String transactionId;  @DynamoDbSortKey @DynamoDbAttribute("transaction_id")
  String type;           // CHECKIN | CHECKOUT
  BigDecimal amount;
  String currency;
  String createdAt;
}
```

## Exception → HTTP mapping
| Exception | HTTP |
|---|---|
| AccountNotFoundException | 404 |
| AccountBlockedException | 422 |
| InsufficientBalanceException | 422 |
| CurrencyMismatchException | 422 |
| ConcurrentModificationException | 409 |
| MissingIdempotencyKeyException | 400 |
