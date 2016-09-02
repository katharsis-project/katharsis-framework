package io.katharsis.jpa.query.querydsl;

import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.JpaQueryFactoryBase;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.backend.querydsl.QuerydslQueryImpl;
import io.katharsis.jpa.query.JpaQueryFactory;

public class QuerydslQueryFactory extends JpaQueryFactoryBase implements JpaQueryFactory {

	private QuerydslQueryFactory(MetaLookup metaLookup, EntityManager em) {
		super(metaLookup, em);
	}

	public static QuerydslQueryFactory newInstance(MetaLookup metaLookup, EntityManager em) {
		return new QuerydslQueryFactory(metaLookup, em);
	}

	@Override
	public <T> QuerydslQuery<T> query(Class<T> entityClass) {
		return new QuerydslQueryImpl<>(metaLookup, em, entityClass, virtualAttrs);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> QuerydslQuery<T> query(Class<?> entityClass, String attrName, List<?> entityIds) {
		return new QuerydslQueryImpl(metaLookup, em, entityClass, virtualAttrs, attrName, entityIds);
	}

	public void registerVirtualAttribute(Class<?> targetClass, String attributeName, Type attributeType,
			QuerydslExpressionFactory<?> expressionFactory) {
		virtualAttrs.register(targetClass, attributeName, expressionFactory, attributeType);
	}
}
