package io.katharsis.spring.boot;

import io.katharsis.spring.SpringServiceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonLocatorConfiguration {

    @Bean
    public SpringServiceLocator serviceLocator() {
        return new SpringServiceLocator();
    }
}
