package io.katharsis.jpa.repository.querydsl;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.querydsl.QuerydslQueryFactory;
import io.katharsis.jpa.repository.JpaEntityRepositoryTestBase;

public class JpaEntityRepositoryQuerydslTest extends JpaEntityRepositoryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
