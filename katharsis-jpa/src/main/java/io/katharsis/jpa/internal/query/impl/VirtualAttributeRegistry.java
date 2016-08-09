package io.katharsis.jpa.internal.query.impl;

import java.util.HashMap;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import io.katharsis.jpa.internal.meta.MetaAttributeProjection;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.query.VirtualAssociation;
import io.katharsis.jpa.internal.query.VirtualAttribute;
import io.katharsis.jpa.internal.util.KatharsisAssert;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class VirtualAttributeRegistry {

	private HashMap<MetaAttributeProjection, VirtualAssociation> assocMap = new HashMap<MetaAttributeProjection, VirtualAssociation>();
	private HashMap<MetaAttributeProjection, VirtualAttribute> attrMap = new HashMap<MetaAttributeProjection, VirtualAttribute>();

	public VirtualAttributeRegistry() {

	}

	public From<?, ?> join(IQueryBuilderContext<?> queryCtx, From<?, ?> parentJoin, MetaAttributeProjection attr) {
		checkAssoc(attr);
		KatharsisAssert.assertTrue(assocMap.containsKey(attr));
		VirtualAssociation association = assocMap.get(attr);
		return association.join(queryCtx, parentJoin, attr);
	}

	public Expression<?> getExpression(IQueryBuilderContext<?> queryCtx, From<?, ?> from, MetaAttributeProjection attr) {
		KatharsisAssert.assertNotNull(attr);
		KatharsisAssert.assertTrue(attr.isDerived());
		KatharsisAssert.assertTrue(attrMap.containsKey(attr));
		VirtualAttribute addAttr = attrMap.get(attr);
		return addAttr.getExpression(queryCtx, from, attr);
	}

	public void register(MetaAttributeProjection attr, VirtualAssociation association) {
		checkAssoc(attr);
		assocMap.put(attr, association);
	}

	public void register(MetaAttributeProjection attr, VirtualAttribute addAttr) {
		KatharsisAssert.assertNotNull(attr);
		KatharsisAssert.assertTrue(attr.isDerived());
		attrMap.put(attr, addAttr);
	}

	private void checkAssoc(MetaAttributeProjection attr) {
		KatharsisAssert.assertNotNull(attr);
		KatharsisAssert.assertTrue(attr.isDerived());
		KatharsisAssert.assertTrue(attr.getType() instanceof MetaDataObject);
	}

	public boolean containsAssoc(MetaAttributeProjection attribute) {
		return assocMap.containsKey(attribute);
	}

	public boolean containsAttr(MetaAttributeProjection attribute) {
		return attrMap.containsKey(attribute);
	}

	public boolean isEmpty() {
		return attrMap.isEmpty() && assocMap.isEmpty();
	}
}
