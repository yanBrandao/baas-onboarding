package com.tapajos.baas_account.infrastructure.repository;

import com.tapajos.baas_account.domain.Currency;
import com.tapajos.baas_account.domain.Transaction;
import com.tapajos.baas_account.domain.TransactionType;
import com.tapajos.baas_account.repository.TransactionRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DynamoTransactionRepository implements TransactionRepository {

    private final DynamoDbTable<TransactionEntity> table;

    public DynamoTransactionRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("Transaction", TableSchema.fromBean(TransactionEntity.class));
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = toEntity(transaction);
        table.putItem(r -> r.item(entity)
                .conditionExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                        .expression("attribute_not_exists(account_id)")
                        .build()));
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(String accountId, String transactionId) {
        Key key = Key.builder()
                .partitionValue(accountId)
                .sortValue(transactionId)
                .build();
        TransactionEntity entity = table.getItem(key);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<Transaction> findAllByAccountId(String accountId) {
        return table.query(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(accountId).build()))
                .items()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private TransactionEntity toEntity(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId(transaction.transactionId());
        entity.setAccountId(transaction.accountId());
        entity.setType(transaction.type() != null ? transaction.type().name() : null);
        entity.setAmount(transaction.amount());
        entity.setCurrency(transaction.currency() != null ? transaction.currency().name() : null);
        entity.setCreatedAt(transaction.createdAt());
        return entity;
    }

    private Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getTransactionId(),
                entity.getAccountId(),
                entity.getType() != null ? TransactionType.valueOf(entity.getType()) : null,
                entity.getAmount(),
                entity.getCurrency() != null ? Currency.valueOf(entity.getCurrency()) : null,
                entity.getCreatedAt()
        );
    }
}
