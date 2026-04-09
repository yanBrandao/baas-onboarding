package com.tapajos.baas_account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class BaasAccountApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaasAccountApplication.class, args);
	}

}
