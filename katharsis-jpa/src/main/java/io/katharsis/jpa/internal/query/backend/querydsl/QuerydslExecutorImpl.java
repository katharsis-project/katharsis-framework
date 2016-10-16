package io.katharsis.jpa.internal.query.backend.querydsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.QTuple;
import com.querydsl.jpa.impl.JPAQuery;

import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.query.AbstractQueryExecutorImpl;
import io.katharsis.jpa.query.querydsl.QuerydslExecutor;
import io.katharsis.jpa.query.querydsl.QuerydslTuple;

public class QuerydslExecutorImpl<T> extends AbstractQueryExecutorImpl<T> implements QuerydslExecutor<T> {

	private JPAQuery<T> query;

	public QuerydslExecutorImpl(EntityManager em, MetaDataObject meta, JPAQuery<T> query, int numAutoSelections,
			Map<String, Integer> selectionBindings) {
		super(em, meta, numAutoSelections, selectionBindings);

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

	@Override
	public List<QuerydslTuple> getResultTuples() {
		List<?> results = executeQuery();

		List<QuerydslTuple> tuples = new ArrayList<>();
		for (Object result : results) {
			if (result instanceof Tuple) {
				tuples.add(new QuerydslTupleImpl((Tuple) result, selectionBindings));
			}
			else {
				tuples.add(new ObjectArrayTupleImpl(result));
			}
		}
		return tuples;
	}
}
