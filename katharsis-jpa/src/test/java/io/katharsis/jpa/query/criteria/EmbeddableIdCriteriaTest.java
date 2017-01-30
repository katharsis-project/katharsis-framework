package io.katharsis.jpa.query.criteria;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.EmbeddableIdQueryTestBase;
import io.katharsis.jpa.query.JpaQueryFactory;

public class EmbeddableIdCriteriaTest extends EmbeddableIdQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}

}
