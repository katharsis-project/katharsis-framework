package io.katharsis.jpa.internal;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.ComputedAttributeRegistryImpl;
import io.katharsis.jpa.query.ComputedAttributeRegistry;

public abstract class JpaQueryFactoryBase {

	protected EntityManager em;

	protected ComputedAttributeRegistryImpl computedAttrs;

	protected MetaLookup metaLookup;

	protected JpaQueryFactoryBase(MetaLookup metaLookup, EntityManager em) {
		this.em = em;
		this.metaLookup = metaLookup;
		this.computedAttrs = new ComputedAttributeRegistryImpl(metaLookup);
	}

	public EntityManager getEntityManager() {
		return em;
	}

	public ComputedAttributeRegistry getComputedAttributes() {
		return computedAttrs;
	}
}
