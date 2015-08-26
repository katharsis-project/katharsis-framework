package io.katharsis.example.springboot.simple;

import io.katharsis.example.springboot.simple.filter.SpringBootSampleKatharsisFilter;

import javax.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares Katharsis Filter as a Bean in this configuration class.
 */
@Configuration
public class WebConfig {

    @Bean
    public Filter springBootSampleKatharsisFilter() {
        SpringBootSampleKatharsisFilter filter = new SpringBootSampleKatharsisFilter();
        return filter;
    }

}
