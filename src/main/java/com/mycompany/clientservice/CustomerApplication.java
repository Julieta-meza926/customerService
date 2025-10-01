package com.mycompany.clientservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableCaching
@EnableAsync
@SpringBootApplication
@EnableJpaRepositories(
		basePackages = "com.mycompany.clientservice.repository"
)
public class
CustomerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerApplication.class, args);
	}
}
