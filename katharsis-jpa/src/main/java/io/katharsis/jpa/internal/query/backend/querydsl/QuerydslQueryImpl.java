package io.katharsis.jpa.internal.query.backend.querydsl;

import java.util.List;

import javax.persistence.EntityManager;

import com.querydsl.jpa.impl.JPAQueryFactory;

import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.AbstractJpaQueryImpl;
import io.katharsis.jpa.internal.query.VirtualAttributeRegistry;
import io.katharsis.jpa.query.querydsl.QuerydslQuery;

public class QuerydslQueryImpl<T> extends AbstractJpaQueryImpl<T, QuerydslQueryBackend<T>> implements QuerydslQuery<T> {

	private JPAQueryFactory queryFactory;

	public QuerydslQueryImpl(MetaLookup metaLookup, EntityManager em, Class<T> clazz,
			VirtualAttributeRegistry virtualAttrs) {
		super(metaLookup, em, clazz, virtualAttrs);
		queryFactory = new JPAQueryFactory(em);
	}

	public QuerydslQueryImpl(MetaLookup metaLookup, EntityManager em, Class<?> clazz,
			VirtualAttributeRegistry virtualAttrs, String attrName, List<?> entityIds) {
		super(metaLookup, em, clazz, virtualAttrs, attrName, entityIds);
		queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public QuerydslQueryExecutor<T> buildExecutor() {
		return (QuerydslQueryExecutor<T>) super.buildExecutor();
	}

	protected JPAQueryFactory getQueryFactory() {
		return queryFactory;
	}

	@Override
	protected QuerydslQueryBackend<T> newBackend() {
		return new QuerydslQueryBackend<>(this, clazz, parentEntityClass, parentAttr);
	}

	@Override
	protected QuerydslQueryExecutor<T> newExecutor(QuerydslQueryBackend<T> ctx, int numAutoSelections) {
		return new QuerydslQueryExecutor<>(em, meta, ctx.getQuery(), numAutoSelections);
	}
}
