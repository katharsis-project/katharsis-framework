package io.katharsis.jpa.internal.query;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import io.katharsis.jpa.internal.meta.MetaAttributeProjection;
import io.katharsis.jpa.internal.query.impl.IQueryBuilderContext;

public interface VirtualAttribute<E, A> {

	public Expression<A> getExpression(IQueryBuilderContext<?> queryCtx, From<?, E> from, MetaAttributeProjection attr);

}
