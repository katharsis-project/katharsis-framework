package io.katharsis.example.springboot.simple;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.InheritableServerClientAndLocalSpanState;
import com.github.kristofa.brave.LoggingReporter;
import com.twitter.zipkin.gen.Endpoint;

import io.katharsis.brave.BraveModule;
import io.katharsis.internal.boot.TransactionRunner;
import io.katharsis.jpa.JpaModule;
import io.katharsis.validation.ValidationModule;

@Configuration
public class ModuleConfig {

	@PersistenceUnit(name = "TEST_SPRING")
	private EntityManagerFactory emFactory;

	@PersistenceContext(unitName = "TEST_SPRING")
	private EntityManager em;

	@Autowired
	private TransactionRunner transactionRunner;

	/**
	 * Bean Validation
	 * @return module
	 */
	@Bean
	public ValidationModule validationModule() {
		return ValidationModule.newInstance();
	}

	/**
	 * Basic monitoring setup with Brave
	 * @return module
	 */
	@Bean
	public BraveModule braveModule() {
		String serviceName = "exampleApp";
		Endpoint localEndpoint = Endpoint.builder().serviceName(serviceName).build();
		InheritableServerClientAndLocalSpanState spanState = new InheritableServerClientAndLocalSpanState(localEndpoint);
		Brave.Builder builder = new Brave.Builder(spanState);
		builder = builder.reporter(new LoggingReporter());
		Brave brave = builder.build();
		return BraveModule.newServerModule(brave);
	}

	/**
	 * Expose JPA entities as repositories.
	 * @return module
	 */
	@Bean
	public JpaModule jpaModule() {
		return JpaModule.newServerModule(emFactory, em, transactionRunner);
	}
}
