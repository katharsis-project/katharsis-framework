package io.katharsis.jpa.query;

import javax.persistence.EntityManager;

import io.katharsis.meta.MetaLookup;

public interface JpaQueryFactoryContext {

	EntityManager getEntityManager();

	MetaLookup getMetaLookup();

}
