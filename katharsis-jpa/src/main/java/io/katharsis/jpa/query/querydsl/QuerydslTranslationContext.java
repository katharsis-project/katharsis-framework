package io.katharsis.jpa.query.querydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.queryspec.Direction;

public interface QuerydslTranslationContext<T> {

	JPAQueryFactory getQueryFactory();

	JPAQuery<T> getQuery();

	Path<T> getRoot();

	<P> EntityPath<P> getParentRoot();

	<E> Expression<E> getAttribute(MetaAttributePath attrPath);

	public <E> EntityPath<E> getJoin(MetaAttributePath path);

	void addPredicate(Predicate predicate);

	<E extends Comparable<E>> OrderSpecifier<E> addOrder(Expression<E> expr, Direction dir);

	void addSelection(Expression<?> expression, String name);

	<U> QuerydslTranslationContext<U> castFor(Class<U> type);
}
