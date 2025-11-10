package com.DBMS.iLibrary.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;


// Access URL (default): http://localhost:8080/swagger-ui/index.html

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "iLibrary application APIs", description = "By Himanshu", version = "v1"),
        tags = {
                @Tag(name = "Admin controller APIs"),
                @Tag(name = "Booking Controller APIs"),
                @Tag(name = "Librarian Controller APIs"),
                @Tag(name = "PriceCheckOut Controller APIs"),
                @Tag(name = "Public Controller APIs"),
                @Tag(name = "Seat Controller APIs"),
                @Tag(name = "StripeWebhook Controller APIs"),
                @Tag(name = "Subscription Controller APIs"),
                @Tag(name = "User Controller APIs")
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
    // No bean required — annotations are enough for springdoc-openapi-starter-webmvc-ui (2.x).
    // If you need further customization (servers, contact, license, etc.) you can either:
    //  - add more annotations here, or
    //  - expose an OpenAPI bean (io.swagger.v3.oas.models.OpenAPI) to programmatically adjust values.
}
