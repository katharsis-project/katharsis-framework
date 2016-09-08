package io.katharsis.jpa.internal;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.VirtualAttributeRegistry;

public abstract class JpaQueryFactoryBase {

	protected EntityManager em;
	protected VirtualAttributeRegistry virtualAttrs;
	protected MetaLookup metaLookup;

	protected JpaQueryFactoryBase(MetaLookup metaLookup, EntityManager em) {
		this.em = em;
		this.metaLookup = metaLookup;
		this.virtualAttrs = new VirtualAttributeRegistry(metaLookup);
	}

	public EntityManager getEntityManager() {
		return em;
	}

}
