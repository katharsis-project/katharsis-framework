package io.katharsis.jpa.repository.querydsl;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.querydsl.QuerydslQueryFactory;
import io.katharsis.jpa.repository.JpaRelationshipRepositoryTestBase;

public class JpaRelationshipRepositoryQuerydslTest extends JpaRelationshipRepositoryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}
}
