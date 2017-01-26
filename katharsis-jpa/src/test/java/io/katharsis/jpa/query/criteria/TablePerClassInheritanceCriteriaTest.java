package io.katharsis.jpa.query.criteria;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.TablePerClassInhertitanceQueryTestBase;

public class TablePerClassInheritanceCriteriaTest extends TablePerClassInhertitanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}
}
