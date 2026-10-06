package dev.jpa.allimio;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI carDatabaseOpenAPI() {
        return new OpenAPI().info(new Info().title("알리미오 RESTful API").description("Allimio 관제서비스").version("1.0"));
    }
}