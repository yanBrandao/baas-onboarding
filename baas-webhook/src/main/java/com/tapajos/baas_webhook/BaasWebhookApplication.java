package com.tapajos.baas_webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

import static org.springframework.web.client.RestClient.*;

@EnableKafka
@SpringBootApplication
public class BaasWebhookApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaasWebhookApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public Builder restClientBuilder() {
		return builder();
	}
}
