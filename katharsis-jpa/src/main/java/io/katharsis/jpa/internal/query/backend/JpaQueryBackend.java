package io.katharsis.jpa.internal.query.backend;

import java.util.List;

import javax.persistence.criteria.JoinType;

import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaAttributePath;
import io.katharsis.queryspec.Direction;
import io.katharsis.queryspec.FilterOperator;

public interface JpaQueryBackend<F, O, P, E> {

	public void distinct();

	public F getRoot();

	public void setOrder(List<O> orderSpecListToOrderArray);

	public List<O> getOrderList();

	public O newSort(E expression, Direction dir);

	public void addPredicate(P predicate);

	public E getAttribute(MetaAttributePath attrPath);

	public void addParentPredicate(MetaAttribute primaryKeyAttr);

	public boolean hasManyRootsFetchesOrJoins();

	public void addSelection(E expression, String name);

	public E getExpression(O order);

	public boolean containsRelation(E expression);

	public P buildPredicate(FilterOperator operator, MetaAttributePath path, Object value);

	public P and(List<P> predicates);

	public P not(P predicate);

	public P or(List<P> predicates);

	public Class<?> getJavaElementType(E currentCriteriaPath);

	public E getAttribute(E currentCriteriaPath, MetaAttribute pathElement);

	public E joinSubType(E currentCriteriaPath, Class<?> entityType);

	public E joinMapValue(E currentCriteriaPath, MetaAttribute pathElement, Object key);

	public F doJoin(MetaAttribute targetAttr, JoinType joinType, F parent);
}
