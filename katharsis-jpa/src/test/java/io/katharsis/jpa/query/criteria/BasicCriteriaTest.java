package io.katharsis.jpa.query.criteria;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.BasicQueryTestBase;
import io.katharsis.jpa.query.JpaQueryFactory;

public class BasicCriteriaTest extends BasicQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}
}
