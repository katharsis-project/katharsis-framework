package io.katharsis.example.springboot.simple;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.InheritableServerClientAndLocalSpanState;
import com.twitter.zipkin.gen.Endpoint;

import io.katharsis.brave.BraveModule;
import io.katharsis.core.internal.boot.TransactionRunner;
import io.katharsis.example.springboot.simple.domain.model.ScheduleDto;
import io.katharsis.example.springboot.simple.domain.model.ScheduleEntity;
import io.katharsis.jpa.JpaModule;
import io.katharsis.jpa.JpaRepositoryConfig;
import io.katharsis.jpa.mapping.JpaMapper;
import io.katharsis.jpa.query.Tuple;
import io.katharsis.jpa.query.criteria.JpaCriteriaExpressionFactory;
import io.katharsis.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.katharsis.validation.ValidationModule;
import zipkin.reporter.Reporter;

@Configuration
public class ModuleConfig {

	@Autowired
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

	public final class LoggingReporter implements Reporter<zipkin.Span> {

		private Logger logger = LoggerFactory.getLogger("springboot.simple.traceReporter");

		@Override
		public void report(zipkin.Span span) {
			logger.info(span.toString());
		}
	}

	/**
	 * Expose JPA entities as repositories.
	 * @return module
	 */
	@Bean
	public JpaModule jpaModule() {
		JpaModule module = JpaModule.newServerModule(em, transactionRunner);

		// directly expose entity
		module.addRepository(JpaRepositoryConfig.builder(ScheduleEntity.class).build());

		// additionally expose entity as a mapped dto
		module.addRepository(JpaRepositoryConfig.builder(ScheduleEntity.class, ScheduleDto.class, new ScheduleMapper()).build());
		JpaCriteriaQueryFactory queryFactory = (JpaCriteriaQueryFactory) module.getQueryFactory();

		// register a computed a attribute
		// you may consider QueryDSL or generating the Criteria query objects.
		queryFactory.registerComputedAttribute(ScheduleEntity.class, "upperName", String.class,
				new JpaCriteriaExpressionFactory<From<?, ScheduleEntity>>() {

					@SuppressWarnings({ "rawtypes", "unchecked" })
					@Override
					public Expression<String> getExpression(From<?, ScheduleEntity> entity, CriteriaQuery<?> query) {
						CriteriaBuilder builder = em.getCriteriaBuilder();
						return builder.upper((Expression) entity.get("name"));
					}
				});
		return module;
	}

	class ScheduleMapper implements JpaMapper<ScheduleEntity, ScheduleDto> {

		@Override
		public ScheduleDto map(Tuple tuple) {
			ScheduleDto dto = new ScheduleDto();

			// first entry in tuple is the queried entity (if not configured otherwise)
			ScheduleEntity entity = tuple.get(0, ScheduleEntity.class);
			dto.setId(entity.getId());
			dto.setName(entity.getName());

			// computed attribute available as additional tuple entry
			dto.setUpperName(tuple.get(1, String.class));
			return dto;
		}

		@Override
		public ScheduleEntity unmap(ScheduleDto dto) {
			// get entity from database if already there
			ScheduleEntity entity = em.find(ScheduleEntity.class, dto.getId());
			if (entity == null) {
				entity = new ScheduleEntity();
				entity.setId(dto.getId());
			}
			entity.setName(dto.getName());
			return entity;
		}

	}
}
