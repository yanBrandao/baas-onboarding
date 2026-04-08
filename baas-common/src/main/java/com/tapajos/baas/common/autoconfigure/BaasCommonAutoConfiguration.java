package com.tapajos.baas.common.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapajos.baas.common.config.OnboardingSnsProperties;
import com.tapajos.baas.common.sns.OnboardingSnsPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

import java.net.URI;

@AutoConfiguration
@EnableConfigurationProperties(OnboardingSnsProperties.class)
public class BaasCommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SnsClient snsClient(OnboardingSnsProperties props) {
        return SnsClient.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("accessKey", "secretKey")))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public OnboardingSnsPublisher onboardingSnsPublisher(
            SnsClient snsClient,
            ObjectMapper objectMapper,
            OnboardingSnsProperties props
    ) {
        return new OnboardingSnsPublisher(snsClient, objectMapper, props.getTopicArn());
    }
}
