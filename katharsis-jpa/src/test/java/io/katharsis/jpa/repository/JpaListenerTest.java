package io.katharsis.jpa.repository;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.JpaEntityRepository;
import io.katharsis.jpa.JpaRepositoryConfig;
import io.katharsis.jpa.JpaRepositoryFilterBase;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.AbstractJpaTest;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.querydsl.QuerydslQueryFactory;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.list.ResourceList;

@Transactional
public class JpaListenerTest extends AbstractJpaTest {

	private JpaEntityRepository<TestEntity, Long> repo;

	@Override
	@Before
	public void setup() {
		super.setup();
		repo = new JpaEntityRepository<>(module, JpaRepositoryConfig.create(TestEntity.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() throws InstantiationException, IllegalAccessException {

		JpaRepositoryFilterBase filter = Mockito.spy(new JpaRepositoryFilterBase());
		module.addFilter(filter);

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		ResourceList<TestEntity> list = repo.findAll(querySpec);
		Assert.assertEquals(5, list.size());

		Mockito.verify(filter, Mockito.times(1)).filterQuerySpec(Mockito.eq(repo), Mockito.eq(querySpec));
		Mockito.verify(filter, Mockito.times(1)).filterResults(Mockito.eq(repo), Mockito.eq(querySpec), Mockito.eq(list));
		Mockito.verify(filter, Mockito.times(1)).filterExecutor(Mockito.eq(repo), Mockito.eq(querySpec),
				Mockito.any(JpaQueryExecutor.class));
		Mockito.verify(filter, Mockito.times(1)).filterTuples(Mockito.eq(repo), Mockito.eq(querySpec), Mockito.anyList());
		Mockito.verify(filter, Mockito.times(1)).filterQuery(Mockito.eq(repo), Mockito.eq(querySpec),
				Mockito.any(JpaQuery.class));
	}

	@Test
	public void testaAddRemove() throws InstantiationException, IllegalAccessException {
		JpaRepositoryFilterBase filter = Mockito.spy(new JpaRepositoryFilterBase());
		module.addFilter(filter);
		Assert.assertEquals(1, module.getFilters().size());
		module.removeFilter(filter);
		Assert.assertEquals(0, module.getFilters().size());
	}

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
