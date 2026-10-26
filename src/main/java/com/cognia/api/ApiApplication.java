package com.cognia.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import com.cognia.api.adapter.out.persistence.ReviewSessionJpaEntity;
import com.cognia.api.domain.service.PrerequisiteGraphValidator;

@SpringBootApplication
@EntityScan(basePackageClasses = ReviewSessionJpaEntity.class)
public class ApiApplication {
	@Bean
	PrerequisiteGraphValidator prerequisiteGraphValidator() {
		return new PrerequisiteGraphValidator();
	}

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
