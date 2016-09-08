package io.katharsis.jpa.internal.query.backend.criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.query.AbstractQueryExecutorImpl;
import io.katharsis.jpa.internal.query.QueryUtil;
import io.katharsis.jpa.query.criteria.JpaCriteriaQueryExecutor;

public class JpaCriteriaQueryExecutorImpl<T> extends AbstractQueryExecutorImpl<T> implements JpaCriteriaQueryExecutor<T> {

	private CriteriaQuery<T> query;

	public JpaCriteriaQueryExecutorImpl(EntityManager em, MetaDataObject meta, CriteriaQuery<T> criteriaQuery,
			int numAutoSelections, Map<String, Integer> selectionBindings) {
		super(em, meta, numAutoSelections, selectionBindings);

		this.query = criteriaQuery;
	}

	public CriteriaQuery<T> getQuery() {
		return query;
	}

	@Override
	protected List<?> executeQuery() {
		TypedQuery<T> typedQuery = em.createQuery(query);
		return executeQuery(typedQuery);
	}

	@Override
	protected boolean isCompoundSelection() {
		return query.getSelection().isCompoundSelection();
	}

	@Override
	protected boolean isDistinct() {
		return query.isDistinct();
	}

	@Override
	protected boolean hasManyRootsFetchesOrJoins() {
		return QueryUtil.hasManyRootsFetchesOrJoins(query);
	}

	@Override
	public long getTotalRowCount() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public List<Tuple> getResultTuples() {
		TypedQuery<T> typedQuery = em.createQuery(query);
		List<?> results = executeQuery();
		List<Tuple> tuples = new ArrayList<>();
		for (Object result : results) {
			if (!(result instanceof Object[])) {
				throw new IllegalStateException("not a tuple result: " + result);
			}
			tuples.add(new CriteriaTupleImpl((Object[]) result, selectionBindings));
		}
		return tuples;
	}
}
