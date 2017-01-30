package io.katharsis.jpa.query.querydsl;

import javax.persistence.EntityManager;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;

import io.katharsis.jpa.model.QTestEntity;
import io.katharsis.jpa.model.TestEntity;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.ComputedAttributeTestBase;

public class ComputedAttributeQuerydslTest extends ComputedAttributeTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		QuerydslQueryFactory factory = QuerydslQueryFactory.newInstance();

		factory.registerComputedAttribute(TestEntity.class, ATTR_VIRTUAL_VALUE, String.class,
				new QuerydslExpressionFactory<QTestEntity>() {

					@Override
					public Expression<?> getExpression(QTestEntity test, JPAQuery<?> query) {
						return test.stringValue.toUpperCase();
					}
				});

		return factory;
	}
}
