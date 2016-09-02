package io.katharsis.jpa.internal.query.backend.criteria;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.query.AbstractQueryExecutorImpl;
import io.katharsis.jpa.internal.query.QueryUtil;
import io.katharsis.jpa.query.criteria.JpaCriteriaQueryExecutor;

public class JpaCriteriaQueryExecutorImpl<T> extends AbstractQueryExecutorImpl<T>
		implements JpaCriteriaQueryExecutor<T> {

	private CriteriaQuery<T> query;

	public JpaCriteriaQueryExecutorImpl(EntityManager em, MetaDataObject meta, CriteriaQuery<T> criteriaQuery,
			int numAutoSelections) {
		super(em, meta, numAutoSelections);

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
}
