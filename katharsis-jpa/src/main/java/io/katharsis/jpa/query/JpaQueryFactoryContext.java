package io.katharsis.jpa.query;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.meta.MetaLookup;

public interface JpaQueryFactoryContext {

	EntityManager getEntityManager();

	MetaLookup getMetaLookup();

}
