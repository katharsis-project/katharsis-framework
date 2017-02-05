package io.katharsis.spring.legacy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonLocatorConfiguration {

    @Bean
    public SpringServiceLocator serviceLocator() {
        return new SpringServiceLocator();
    }
}
