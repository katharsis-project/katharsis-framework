package io.katharsis.jpa.internal.query.backend.querydsl;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.querydsl.jpa.impl.JPAQueryFactory;

import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.AbstractJpaQueryImpl;
import io.katharsis.jpa.internal.query.ComputedAttributeRegistryImpl;
import io.katharsis.jpa.query.querydsl.QuerydslQuery;

public class QuerydslQueryImpl<T> extends AbstractJpaQueryImpl<T, QuerydslQueryBackend<T>> implements QuerydslQuery<T> {

	private JPAQueryFactory queryFactory;

	public QuerydslQueryImpl(MetaLookup metaLookup, EntityManager em, Class<T> clazz,
			ComputedAttributeRegistryImpl computedAttrs) {
		super(metaLookup, em, clazz, computedAttrs);
		queryFactory = new JPAQueryFactory(em);
	}

	public QuerydslQueryImpl(MetaLookup metaLookup, EntityManager em, Class<?> clazz,
			ComputedAttributeRegistryImpl virtualAttrs, String attrName, List<?> entityIds) {
		super(metaLookup, em, clazz, virtualAttrs, attrName, entityIds);
		queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public QuerydslExecutorImpl<T> buildExecutor() {
		return (QuerydslExecutorImpl<T>) super.buildExecutor();
	}

	protected JPAQueryFactory getQueryFactory() {
		return queryFactory;
	}

	@Override
	protected QuerydslQueryBackend<T> newBackend() {
		return new QuerydslQueryBackend<>(this, clazz, parentEntityClass, parentAttr, parentIdSelection);
	}

	@Override
	protected QuerydslExecutorImpl<T> newExecutor(QuerydslQueryBackend<T> ctx, int numAutoSelections, Map<String, Integer> selectionBindings) {
		return new QuerydslExecutorImpl<>(em, meta, ctx.getQuery(), numAutoSelections, selectionBindings);
	}
}
