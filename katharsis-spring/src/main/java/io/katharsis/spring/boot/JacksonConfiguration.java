package io.katharsis.spring.boot;

import com.fasterxml.jackson.databind.Module;
import io.katharsis.jackson.JsonApiModuleBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Module parameterNamesModule() {
        return JsonApiModuleBuilder.create();
    }
}
