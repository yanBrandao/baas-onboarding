package com.tapajos.baas_account.infrastructure.repository;

import com.tapajos.baas_account.domain.Account;
import com.tapajos.baas_account.domain.AccountStatus;
import com.tapajos.baas_account.domain.Currency;
import com.tapajos.baas_account.repository.AccountRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public class DynamoAccountRepository implements AccountRepository {

    private final DynamoDbTable<AccountEntity> table;

    public DynamoAccountRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("Account", TableSchema.fromBean(AccountEntity.class));
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = toEntity(account);
        table.putItem(entity);
        return account;
    }

    @Override
    public Optional<Account> findById(String accountId) {
        Key key = Key.builder().partitionValue(accountId).build();
        AccountEntity entity = table.getItem(key);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<Account> updateBalance(Account account, BigDecimal newBalance) {
        AccountEntity patch = new AccountEntity();
        patch.setAccountId(account.accountId());
        patch.setBalance(newBalance);
        patch.setVersion(account.version());

        AccountEntity updated = table.updateItem(r -> r.item(patch).ignoreNulls(true));
        return Optional.ofNullable(updated).map(this::toDomain);
    }

    @Override
    public Optional<Account> updateStatus(String accountId, String status, Long version) {
        AccountEntity patch = new AccountEntity();
        patch.setAccountId(accountId);
        patch.setStatus(status);
        patch.setVersion(version);

        AccountEntity updated = table.updateItem(r -> r.item(patch).ignoreNulls(true));
        return Optional.ofNullable(updated).map(this::toDomain);
    }

    private AccountEntity toEntity(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.setAccountId(account.accountId());
        entity.setOnboardingId(account.onboardingId());
        entity.setCurrency(account.currency() != null ? account.currency().name() : null);
        entity.setBalance(account.balance());
        entity.setStatus(account.status() != null ? account.status().name() : null);
        entity.setVersion(account.version());
        entity.setCreatedAt(account.createdAt());
        return entity;
    }

    private Account toDomain(AccountEntity entity) {
        return new Account(
                entity.getAccountId(),
                entity.getOnboardingId(),
                entity.getCurrency() != null ? Currency.valueOf(entity.getCurrency()) : null,
                entity.getBalance(),
                entity.getStatus() != null ? AccountStatus.valueOf(entity.getStatus()) : null,
                entity.getVersion(),
                entity.getCreatedAt()
        );
    }
}
