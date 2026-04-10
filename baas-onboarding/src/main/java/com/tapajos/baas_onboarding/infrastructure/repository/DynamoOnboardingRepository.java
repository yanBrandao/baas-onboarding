package com.tapajos.baas_onboarding.infrastructure.repository;

import com.tapajos.baas_onboarding.domain.Address;
import com.tapajos.baas_onboarding.domain.Onboarding;
import com.tapajos.baas_onboarding.repository.OnboardingRepository;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class DynamoOnboardingRepository implements OnboardingRepository {

    private final DynamoDbTable<OnboardingEntity> table;

    public DynamoOnboardingRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table("Onboarding", TableSchema.fromBean(OnboardingEntity.class));
    }

    @Override
    @Observed(
            name = "baas.dynamodb.onboarding.save",
            contextualName = "save onboarding to dynamodb",
            lowCardinalityKeyValues = {"db.system", "dynamodb", "db.operation", "PutItem", "db.name", "Onboarding"}
    )
    public void save(Onboarding onboarding) {
        table.putItem(toEntity(onboarding));
    }

    @Override
    @Observed(
            name = "baas.dynamodb.onboarding.find",
            contextualName = "load onboarding from dynamodb",
            lowCardinalityKeyValues = {"db.system", "dynamodb", "db.operation", "GetItem", "db.name", "Onboarding"}
    )
    public Optional<Onboarding> findById(String onboardingId) {
        Key key = Key.builder().partitionValue(onboardingId).build();
        OnboardingEntity entity = table.getItem(key);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    @Observed(
            name = "baas.dynamodb.onboarding.update",
            contextualName = "update onboarding in dynamodb",
            lowCardinalityKeyValues = {"db.system", "dynamodb", "db.operation", "UpdateItem", "db.name", "Onboarding"}
    )
    public Optional<Onboarding> updateStatus(String onboardingId, String status) {
        OnboardingEntity patch = new OnboardingEntity();
        patch.setOnboardingId(onboardingId);
        patch.setStatus(status);

        OnboardingEntity updated = table.updateItem(r -> r.item(patch).ignoreNulls(true));
        return Optional.ofNullable(updated).map(this::toDomain);
    }

    private OnboardingEntity toEntity(Onboarding onboarding) {
        OnboardingEntity entity = new OnboardingEntity();
        entity.setOnboardingId(onboarding.onboardingId());
        entity.setStatus(onboarding.status());
        entity.setName(onboarding.name());
        entity.setEmail(onboarding.email());
        entity.setPhone(onboarding.phone());
        entity.setDocument(onboarding.document());
        entity.setBirthDate(onboarding.birthDate());
        entity.setMotherName(onboarding.motherName());
        entity.setFingerprint(onboarding.fingerprint());
        if (onboarding.address() != null) {
            AddressEntity addressEntity = new AddressEntity();
            addressEntity.setStreet(onboarding.address().street());
            addressEntity.setCity(onboarding.address().city());
            addressEntity.setState(onboarding.address().state());
            addressEntity.setZip(onboarding.address().zip());
            entity.setAddress(addressEntity);
        }
        return entity;
    }

    private Onboarding toDomain(OnboardingEntity entity) {
        Address address = null;
        if (entity.getAddress() != null) {
            address = new Address(
                    entity.getAddress().getStreet(),
                    entity.getAddress().getCity(),
                    entity.getAddress().getState(),
                    entity.getAddress().getZip()
            );
        }
        return new Onboarding(
                entity.getOnboardingId(),
                entity.getStatus(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getDocument(),
                entity.getBirthDate(),
                entity.getMotherName(),
                entity.getFingerprint(),
                address
        );
    }
}
