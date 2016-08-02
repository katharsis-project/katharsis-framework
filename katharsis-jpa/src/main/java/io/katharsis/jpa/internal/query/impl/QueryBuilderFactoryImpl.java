package io.katharsis.jpa.internal.query.impl;

import java.util.List;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.query.QueryBuilder;
import io.katharsis.jpa.internal.query.QueryBuilderFactory;

public class QueryBuilderFactoryImpl implements QueryBuilderFactory {

	private EntityManager em;

	public QueryBuilderFactoryImpl(EntityManager em) {
		this.em = em;
	}

	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	public <T> QueryBuilderImpl<T> newBuilder(Class<T> entityClass) {
		return new QueryBuilderImpl<T>(em, entityClass);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> QueryBuilder<T> newBuilder(Class<?> entityClass, String attrName, List<?> entityIds) {
		return new QueryBuilderImpl(em, entityClass, attrName, entityIds);
	}
}
