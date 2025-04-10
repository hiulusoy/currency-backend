package com.crewmeister.currencybackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuration for Swagger/OpenAPI documentation
 */
@Configuration
public class OpenAPIConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.port:8088}")
    private String serverPort;

    /**
     * Configure the OpenAPI documentation with detailed application information
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .servers(Arrays.asList(
                        new Server().url("http://localhost:" + serverPort + contextPath)
                                .description("Local Development Server"),
                        new Server().url("https://api.crewmeister.com" + contextPath)
                                .description("Production Server")));
    }

    private Info apiInfo() {
        return new Info()
                .title("Currency Exchange API")
                .description("REST API for retrieving and managing currency exchange rates")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Crewmeister Development Team")
                        .email("dev@crewmeister.com")
                        .url("https://crewmeister.com"));
    }
}
