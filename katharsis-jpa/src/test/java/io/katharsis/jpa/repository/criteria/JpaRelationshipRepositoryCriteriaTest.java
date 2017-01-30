package io.katharsis.jpa.repository.criteria;

import javax.persistence.EntityManager;

import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.katharsis.jpa.repository.JpaRelationshipRepositoryTestBase;

public class JpaRelationshipRepositoryCriteriaTest extends JpaRelationshipRepositoryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}
}
