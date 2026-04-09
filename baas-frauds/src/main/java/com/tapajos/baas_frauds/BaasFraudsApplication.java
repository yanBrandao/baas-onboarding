package com.tapajos.baas_frauds;

import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableKafka
@SpringBootApplication
public class BaasFraudsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaasFraudsApplication.class, args);
	}

}
