package io.katharsis.jpa.repository;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

import io.katharsis.jpa.JpaEntityRepository;
import io.katharsis.jpa.JpaRepositoryFilter;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.AbstractJpaTest;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.Tuple;
import io.katharsis.jpa.query.querydsl.QuerydslQueryFactory;
import io.katharsis.queryspec.QuerySpec;

@Transactional
public class JpaListenerTest extends AbstractJpaTest {

	private JpaEntityRepository<TestEntity, Long> repo;

	@Override
	@Before
	public void setup() {
		super.setup();
		repo = new JpaEntityRepository<>(module, TestEntity.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() throws InstantiationException, IllegalAccessException {

		TestFilter filter = Mockito.spy(new TestFilter());
		module.addFilter(filter);

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		List<TestEntity> list = repo.findAll(querySpec);
		Assert.assertEquals(5, list.size());

		Mockito.verify(filter, Mockito.times(1)).filterQuerySpec(Mockito.eq(repo), Mockito.eq(querySpec));
		Mockito.verify(filter, Mockito.times(1)).filterResults(Mockito.eq(repo), Mockito.eq(querySpec), Mockito.eq(list));
		Mockito.verify(filter, Mockito.times(1)).filterExecutor(Mockito.eq(repo), Mockito.eq(querySpec),
				Mockito.any(JpaQueryExecutor.class));
		Mockito.verify(filter, Mockito.times(1)).filterTuples(Mockito.eq(repo), Mockito.eq(querySpec), Mockito.anyList());
		Mockito.verify(filter, Mockito.times(1)).filterQuery(Mockito.eq(repo), Mockito.eq(querySpec),
				Mockito.any(JpaQuery.class));
	}

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance(new MetaLookup(), em);
	}

	class TestFilter implements JpaRepositoryFilter {

		@Override
		public boolean accept(Class<?> resourceType) {
			return true;
		}

		@Override
		public QuerySpec filterQuerySpec(Object repository, QuerySpec querySpec) {
			return querySpec;
		}

		@Override
		public <T> JpaQuery<T> filterQuery(Object repository, QuerySpec querySpec, JpaQuery<T> query) {
			return query;
		}

		@Override
		public <T> JpaQueryExecutor<T> filterExecutor(Object repository, QuerySpec querySpec, JpaQueryExecutor<T> executor) {
			return executor;
		}

		@Override
		public List<Tuple> filterTuples(Object repository, QuerySpec querySpec, List<Tuple> tuples) {
			return tuples;
		}

		@Override
		public <T> List<T> filterResults(Object repository, QuerySpec querySpec, List<T> resources) {
			return resources;
		}
	}
}
