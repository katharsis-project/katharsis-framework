package io.katharsis.jpa.query.criteria;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.JoinedTableInheritanceQueryTestBase;
import io.katharsis.jpa.query.JpaQueryFactory;

public class JoinedTableInheritanceCriteriaTest extends JoinedTableInheritanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}

}
