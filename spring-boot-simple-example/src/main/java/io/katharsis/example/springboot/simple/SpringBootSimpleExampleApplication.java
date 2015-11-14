package io.katharsis.example.springboot.simple;

import io.katharsis.spring.boot.KatharsisConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(KatharsisConfig.class)
public class SpringBootSimpleExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootSimpleExampleApplication.class, args);
    }
}
