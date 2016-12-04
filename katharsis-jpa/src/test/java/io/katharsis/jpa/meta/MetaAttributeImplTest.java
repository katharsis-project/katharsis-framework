package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaCollectionType;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.meta.MetaMapType;
import io.katharsis.jpa.model.RelatedEntity;
import io.katharsis.jpa.model.TestEntity;

public class MetaAttributeImplTest {

	@Test
	public void testPrimaryKey() {
		MetaLookup lookup = new MetaLookup();
		MetaEntity meta = lookup.getMeta(TestEntity.class).asEntity();
		MetaAttribute attr = meta.getAttribute("id");
		Assert.assertFalse(attr.isAssociation());
		Assert.assertEquals("id", attr.getName());
		Assert.assertEquals(TestEntity.class.getName() + ".id", attr.getId());
		Assert.assertFalse(attr.isDerived());
		Assert.assertFalse(attr.isVersion());
		Assert.assertFalse(attr.isLazy());
		Assert.assertTrue(attr.isId());
		Assert.assertNull(attr.getOppositeAttribute());
	}

	@Test
	public void testMapAttr() {
		MetaLookup lookup = new MetaLookup();
		MetaEntity meta = lookup.getMeta(TestEntity.class).asEntity();
		MetaAttribute attr = meta.getAttribute(TestEntity.ATTR_mapValue);
		Assert.assertFalse(attr.isAssociation());
		Assert.assertEquals(TestEntity.ATTR_mapValue, attr.getName());
		Assert.assertEquals(TestEntity.class.getName() + "." + TestEntity.ATTR_mapValue, attr.getId());
		Assert.assertFalse(attr.isDerived());
		Assert.assertFalse(attr.isVersion());
		Assert.assertFalse(attr.isLazy());
		Assert.assertFalse(attr.isId());
		Assert.assertNull(attr.getOppositeAttribute());

		MetaMapType mapType = attr.getType().asMap();
		Assert.assertTrue(mapType.isMap());
		Assert.assertEquals(String.class, mapType.getKeyType().getImplementationClass());
		Assert.assertEquals(String.class, mapType.getValueType().getImplementationClass());
		Assert.assertEquals(String.class, attr.getType().getElementType().getImplementationClass());
	}

	@Test
	public void testRelationMany() {
		MetaLookup lookup = new MetaLookup();
		MetaEntity meta = lookup.getMeta(TestEntity.class).asEntity();
		MetaAttribute attr = meta.getAttribute(TestEntity.ATTR_manyRelatedValues);
		Assert.assertTrue(attr.isAssociation());
		Assert.assertEquals(TestEntity.ATTR_manyRelatedValues, attr.getName());
		Assert.assertEquals(TestEntity.class.getName() + "." + TestEntity.ATTR_manyRelatedValues, attr.getId());
		Assert.assertFalse(attr.isDerived());
		Assert.assertFalse(attr.isVersion());
		Assert.assertTrue(attr.isLazy());
		Assert.assertFalse(attr.isId());
		Assert.assertNotNull(attr.getOppositeAttribute());

		MetaCollectionType colType = attr.getType().asCollection();
		Assert.assertTrue(colType.isCollection());
		Assert.assertEquals(RelatedEntity.class, colType.getElementType().getImplementationClass());
		Assert.assertEquals(RelatedEntity.class, attr.getType().getElementType().getImplementationClass());
	}
}
