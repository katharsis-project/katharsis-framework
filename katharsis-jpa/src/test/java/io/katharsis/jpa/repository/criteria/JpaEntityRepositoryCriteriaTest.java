package io.katharsis.jpa.repository.criteria;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.katharsis.jpa.repository.JpaEntityRepositoryTestBase;

public class JpaEntityRepositoryCriteriaTest extends JpaEntityRepositoryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}
}
