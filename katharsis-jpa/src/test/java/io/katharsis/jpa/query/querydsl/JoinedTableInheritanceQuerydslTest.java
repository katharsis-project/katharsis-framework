package io.katharsis.jpa.query.querydsl;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.JoinedTableInheritanceQueryTestBase;
import io.katharsis.jpa.query.JpaQueryFactory;

public class JoinedTableInheritanceQuerydslTest extends JoinedTableInheritanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
