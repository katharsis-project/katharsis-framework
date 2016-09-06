package io.katharsis.jpa.internal.query.backend.querydsl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.querydsl.core.types.QTuple;
import com.querydsl.jpa.impl.JPAQuery;

import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.query.AbstractQueryExecutorImpl;

public class QuerydslQueryExecutor<T> extends AbstractQueryExecutorImpl<T> {

	private JPAQuery<T> query;

	public QuerydslQueryExecutor(EntityManager em, MetaDataObject meta, JPAQuery<T> query, int numAutoSelections) {
		super(em, meta, numAutoSelections);

		this.query = query;
	}

	public JPAQuery<T> getQuery() {
		return query;
	}

	@Override
	protected List<?> executeQuery() {
		Query jpaQuery = query.createQuery();
		return executeQuery(jpaQuery);
	}

	@Override
	protected boolean isCompoundSelection() {
		return query.getMetadata().getProjection() instanceof QTuple;
	}

	@Override
	protected boolean isDistinct() {
		return query.getMetadata().isDistinct();
	}

	@Override
	protected boolean hasManyRootsFetchesOrJoins() {
		return QuerydslUtils.hasManyRootsFetchesOrJoins(query);
	}

	/**
	 * Returns the row count for the query.
	 */
	@Override
	public long getTotalRowCount() {
		return query.fetchCount();
	}
}
