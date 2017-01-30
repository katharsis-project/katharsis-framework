package io.katharsis.jpa.query.criteria;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.ComputedAttributeTestBase;

public class ComputedAttributeCriteriaTest extends ComputedAttributeTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(final EntityManager em) {
		JpaCriteriaQueryFactory factory = JpaCriteriaQueryFactory.newInstance();

		factory.registerComputedAttribute(TestEntity.class, ATTR_VIRTUAL_VALUE, String.class,
				new JpaCriteriaExpressionFactory<From<?, TestEntity>>() {

					@Override
					public Expression<String> getExpression(From<?, TestEntity> parent, CriteriaQuery<?> query) {
						CriteriaBuilder builder = em.getCriteriaBuilder();
						Path<String> stringValue = parent.get(TestEntity.ATTR_stringValue);
						return builder.upper(stringValue);
					}
				});

		return factory;
	}
}
