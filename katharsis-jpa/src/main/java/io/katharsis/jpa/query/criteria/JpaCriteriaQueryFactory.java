package io.katharsis.jpa.query.criteria;

import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.JpaQueryFactoryBase;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.backend.criteria.JpaCriteriaQueryImpl;
import io.katharsis.jpa.query.JpaQueryFactory;

public class JpaCriteriaQueryFactory extends JpaQueryFactoryBase implements JpaQueryFactory {

	private JpaCriteriaQueryFactory(MetaLookup metaLookup, EntityManager em) {
		super(metaLookup, em);
	}

	public static JpaCriteriaQueryFactory newInstance(MetaLookup metaLookup, EntityManager em) {
		return new JpaCriteriaQueryFactory(metaLookup, em);
	}

	@Override
	public <T> JpaCriteriaQuery<T> query(Class<T> entityClass) {
		return new JpaCriteriaQueryImpl<>(metaLookup, em, entityClass, computedAttrs);
	}

	@Override
	public <T> JpaCriteriaQuery<T> query(Class<?> entityClass, String attrName, List<?> entityIds) {
		return new JpaCriteriaQueryImpl<>(metaLookup, em, entityClass, computedAttrs, attrName, entityIds);
	}

	public void registerComputedAttribute(Class<?> targetClass, String attributeName, Type attributeType,
			JpaCriteriaExpressionFactory<?> expressionFactory) {
		computedAttrs.register(targetClass, attributeName, expressionFactory, attributeType);
	}

}
