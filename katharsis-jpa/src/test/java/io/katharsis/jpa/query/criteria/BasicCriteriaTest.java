package io.katharsis.jpa.query.criteria;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.internal.query.backend.criteria.JpaCriteriaQueryExecutorImpl;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.BasicQueryTestBase;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryFactory;

public class BasicCriteriaTest extends BasicQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}

	@Test
	public void testSetCached() {
		JpaQuery<TestEntity> builder = queryFactory.query(TestEntity.class);
		JpaCriteriaQueryExecutorImpl<TestEntity> executor = (JpaCriteriaQueryExecutorImpl<TestEntity>) builder.buildExecutor();
		executor.setCached(true);
		TypedQuery<TestEntity> typedQuery = executor.getTypedQuery();
		Map<String, Object> hints = typedQuery.getHints();
		Assert.assertTrue(hints.containsKey("org.hibernate.cacheable"));
	}
}
