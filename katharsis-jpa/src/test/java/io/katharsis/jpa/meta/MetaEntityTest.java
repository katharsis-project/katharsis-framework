package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.model.TestMappedSuperclassWithPk;
import io.katharsis.jpa.model.TestSubclassWithSuperclassPk;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaKey;

public class MetaEntityTest {

	@Test
	public void testPrimaryKeyOnParentMappedSuperClass() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		MetaEntity meta = lookup.getMeta(TestSubclassWithSuperclassPk.class, MetaEntity.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
	}

	@Test
	public void testPrimaryKeyOnMappedSuperClass() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		MetaMappedSuperclass meta = lookup.getMeta(TestMappedSuperclassWithPk.class, MetaMappedSuperclass.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
	}
}
