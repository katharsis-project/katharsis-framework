package io.katharsis.jpa.query.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;

public interface QuerydslExpressionFactory<T extends Expression<?>> {

	public Expression<?> getExpression(T parent, JPAQuery<?> jpaQuery);
}
