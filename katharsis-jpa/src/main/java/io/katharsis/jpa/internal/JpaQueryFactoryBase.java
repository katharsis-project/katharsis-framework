package io.katharsis.jpa.internal;

import javax.persistence.EntityManager;

import io.katharsis.jpa.internal.query.ComputedAttributeRegistryImpl;
import io.katharsis.jpa.query.ComputedAttributeRegistry;
import io.katharsis.jpa.query.JpaQueryFactory;
import io.katharsis.jpa.query.JpaQueryFactoryContext;
import io.katharsis.meta.MetaLookup;

public abstract class JpaQueryFactoryBase implements JpaQueryFactory {

	protected EntityManager em;

	protected ComputedAttributeRegistryImpl computedAttrs = new ComputedAttributeRegistryImpl();

	protected MetaLookup metaLookup;
	

	@Override
	public void initalize(JpaQueryFactoryContext context) {
		this.em = context.getEntityManager();
		this.metaLookup = context.getMetaLookup();
		this.computedAttrs.init(context);
	}

	public EntityManager getEntityManager() {
		return em;
	}

	public ComputedAttributeRegistry getComputedAttributes() {
		return computedAttrs;
	}
}
