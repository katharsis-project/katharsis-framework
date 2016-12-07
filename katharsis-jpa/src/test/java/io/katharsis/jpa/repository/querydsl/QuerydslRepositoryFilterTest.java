package io.katharsis.jpa.repository.querydsl;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.Test;
import org.mockito.Mockito;

import io.katharsis.jpa.JpaEntityRepository;
import io.katharsis.jpa.JpaRepositoryConfig;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.AbstractJpaTest;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.querydsl.QuerydslQueryFactory;
import io.katharsis.jpa.query.querydsl.QuerydslRepositoryFilterBase;
import io.katharsis.jpa.query.querydsl.QuerydslTranslationContext;
import io.katharsis.queryspec.QuerySpec;

@Transactional
public class QuerydslRepositoryFilterTest extends AbstractJpaTest {

	@SuppressWarnings("unchecked")
	@Test
	public void translationInterceptor() {
		JpaEntityRepository<TestEntity, Long> repo = new JpaEntityRepository<>(module, JpaRepositoryConfig.create(TestEntity.class));
		QuerydslRepositoryFilterBase filter = Mockito.spy(new QuerydslRepositoryFilterBase());
		module.addFilter(filter);

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		repo.findAll(querySpec);

		Mockito.verify(filter, Mockito.times(1)).filterQueryTranslation(Mockito.eq(repo), Mockito.eq(querySpec),
				Mockito.any(QuerydslTranslationContext.class));
	}

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}
}
