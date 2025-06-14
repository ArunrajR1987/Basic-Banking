package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@OpenAPIDefinition(info = @Info(
    title = "PITA Banking API",
    version = "1.0",
    description = "API documentation for PITA Banking Application"
))
public class PitaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PitaApplication.class, args);
	}

}