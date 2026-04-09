package com.tapajos.baas.common.autoconfigure;

import com.tapajos.baas.common.config.OnboardingSnsProperties;
import com.tapajos.baas.common.config.OnboardingSqsProperties;
import com.tapajos.baas.common.sns.OnboardingSnsPublisher;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@AutoConfiguration
@EnableConfigurationProperties({OnboardingSnsProperties.class, OnboardingSqsProperties.class})
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
            OnboardingSnsProperties props
    ) {
        return new OnboardingSnsPublisher(snsClient, props.getTopicArn());
    }

    @Bean
    @ConditionalOnMissingBean
    public SqsAsyncClient sqsAsyncClient(OnboardingSqsProperties props) {
        return SqsAsyncClient.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("accessKey", "secretKey")))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient
    ) {
        return SqsMessageListenerContainerFactory.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }

}
