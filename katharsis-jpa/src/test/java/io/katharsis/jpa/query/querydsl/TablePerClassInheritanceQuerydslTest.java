package io.katharsis.jpa.query.querydsl;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.TablePerClassInhertitanceQueryTestBase;

public class TablePerClassInheritanceQuerydslTest extends TablePerClassInhertitanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}
}
