package io.katharsis.example.springboot.simple;

import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.spring.boot.KatharsisConfigV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Configuration
@RestController
@SpringBootApplication
@Import(KatharsisConfigV2.class)
public class SpringBootSimpleExampleApplication {

    @Autowired
    private ResourceRegistry resourceRegistry;

    @RequestMapping("/resourcesInfo")
    public Map<?, ?> getResources(){
        return resourceRegistry.getResources();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootSimpleExampleApplication.class, args);
    }
}
