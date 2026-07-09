package com.loyaltyplatform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI-Powered Customer Loyalty Platform API")
                        .description("""
                                Production-grade SaaS platform for customer loyalty management,
                                marketing automation, and AI-powered campaign generation.
                                
                                **Features:**
                                - JWT Authentication with refresh tokens
                                - Customer lifecycle management
                                - Loyalty points & tier system
                                - AI-generated campaigns via Groq
                                - Real-time analytics
                                - Coupon & promotion engine
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Platform Team")
                                .email("api@loyaltyplatform.com"))
                        .license(new License()
                                .name("MIT License")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development"),
                        new Server().url("https://loyalty-backend.onrender.com").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token")));
    }
}
