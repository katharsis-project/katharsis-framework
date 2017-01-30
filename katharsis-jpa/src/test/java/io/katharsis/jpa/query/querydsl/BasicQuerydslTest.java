package io.katharsis.jpa.query.querydsl;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.BasicQueryTestBase;
import io.katharsis.jpa.query.JpaQueryFactory;

public class BasicQuerydslTest extends BasicQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}
}
