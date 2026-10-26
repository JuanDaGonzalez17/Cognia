package com.cognia.api.adapter.in.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
	private static final String BEARER_AUTH = "BearerAuth";

	@Bean
	public OpenAPI cogniaOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Cognia Adaptive Learning API")
						.version("1.0")
						.description("API para aprendizaje adaptativo, práctica, evaluación y repaso espaciado."))
				.components(new Components().addSecuritySchemes(BEARER_AUTH,
						new SecurityScheme()
							.type(SecurityScheme.Type.HTTP)
							.scheme("bearer")
							.bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
	}
}
