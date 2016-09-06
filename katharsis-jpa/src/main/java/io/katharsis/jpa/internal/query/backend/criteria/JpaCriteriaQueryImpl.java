package io.katharsis.jpa.internal.query.backend.criteria;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.AbstractJpaQueryImpl;
import io.katharsis.jpa.internal.query.VirtualAttributeRegistry;
import io.katharsis.jpa.query.criteria.JpaCriteriaQuery;

public class JpaCriteriaQueryImpl<T> extends AbstractJpaQueryImpl<T, JpaCriteriaQueryBackend<T>>
		implements JpaCriteriaQuery<T> {

	public JpaCriteriaQueryImpl(MetaLookup metaLookup, EntityManager em, Class<T> clazz,
			VirtualAttributeRegistry virtualAttrs) {
		super(metaLookup, em, clazz, virtualAttrs);
	}

	public JpaCriteriaQueryImpl(MetaLookup metaLookup, EntityManager em, Class<?> clazz,
			VirtualAttributeRegistry virtualAttrs, String attrName, List<?> entityIds) {
		super(metaLookup, em, clazz, virtualAttrs, attrName, entityIds);
	}

	public CriteriaQuery<T> buildQuery() {
		return buildExecutor().getQuery();
	}

	@Override
	public JpaCriteriaQueryExecutorImpl<T> buildExecutor() {
		return (JpaCriteriaQueryExecutorImpl<T>) super.buildExecutor();
	}

	@Override
	protected JpaCriteriaQueryBackend<T> newBackend() {
		return new JpaCriteriaQueryBackend<>(this, em, clazz, parentEntityClass, parentAttr);
	}

	@Override
	protected JpaCriteriaQueryExecutorImpl<T> newExecutor(JpaCriteriaQueryBackend<T> ctx, int numAutoSelections) {
		return new JpaCriteriaQueryExecutorImpl<>(em, meta, ctx.getCriteriaQuery(), numAutoSelections);
	}
}
