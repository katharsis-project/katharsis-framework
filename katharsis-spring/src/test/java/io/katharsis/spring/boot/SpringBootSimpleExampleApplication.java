package io.katharsis.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.katharsis.spring.legacy.KatharsisConfig;
import io.katharsis.spring.legacy.KatharsisConfigV2;

@RestController
@SpringBootApplication
@ComponentScan(value = "io.katharsis.spring",
		excludeFilters = @ComponentScan.Filter(classes = { KatharsisConfig.class, KatharsisConfigV2.class, KatharsisSpringBootProperties.class },
				type = FilterType.ASSIGNABLE_TYPE))
public class SpringBootSimpleExampleApplication {

	@RequestMapping("/api/custom")
	public String customMethod() {
		return "hello";
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSimpleExampleApplication.class, args);
	}
}
