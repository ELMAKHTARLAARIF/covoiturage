package org.com.covoiturage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI covoiturageOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("API Covoiturage multi-étapes")
                .version("1.0")
                .description("Gestion des conducteurs, passagers, véhicules, trajets et réservations"));
    }
}