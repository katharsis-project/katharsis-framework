package io.katharsis.jpa.query.querydsl;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.EmbeddableIdQueryTestBase;
import io.katharsis.jpa.query.JpaQueryFactory;

public class EmbeddableIdQuerydslTest extends EmbeddableIdQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
