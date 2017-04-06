package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.model.NonJpaChildEntity;
//import io.katharsis.jpa.model.SequenceEntity;
import io.katharsis.jpa.model.TestMappedSuperclassWithPk;
import io.katharsis.jpa.model.TestSubclassWithSuperclassPk;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaPrimaryKey;

public class MetaEntityTest {

	@Test
	public void testPrimaryKeyOnParentMappedSuperClass() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		MetaEntity meta = lookup.getMeta(TestSubclassWithSuperclassPk.class, MetaEntity.class);
		MetaPrimaryKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertFalse(primaryKey.isGenerated());
	}

	@Test
	public void testPrimaryKeyOnMappedSuperClass() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		MetaMappedSuperclass meta = lookup.getMeta(TestMappedSuperclassWithPk.class, MetaMappedSuperclass.class);
		MetaPrimaryKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertFalse(primaryKey.isGenerated());
	}

	@Test
	public void testNonJpaSuperTypeMustBeIgnored() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		MetaEntity meta = lookup.getMeta(NonJpaChildEntity.class, MetaEntity.class);
		MetaPrimaryKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertNotNull(meta.getAttribute("intValue"));

		Assert.assertFalse(meta.hasAttribute("nonJpaValue"));
		Assert.assertNull(meta.getSuperType());
	}
}
