package io.katharsis.jpa.internal.query;

import javax.persistence.criteria.From;

import io.katharsis.jpa.internal.meta.MetaAttributeProjection;
import io.katharsis.jpa.internal.query.impl.IQueryBuilderContext;

public interface VirtualAssociation<E, A> {

	public From<?, A> join(IQueryBuilderContext<?> queryCtx, From<?, E> parentJoin, MetaAttributeProjection attr);

}
