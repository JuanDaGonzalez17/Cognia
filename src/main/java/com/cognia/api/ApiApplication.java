package com.cognia.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.cognia.api.domain.service.PrerequisiteGraphValidator;

@SpringBootApplication
public class ApiApplication {
	@Bean
	PrerequisiteGraphValidator prerequisiteGraphValidator() {
		return new PrerequisiteGraphValidator();
	}

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
