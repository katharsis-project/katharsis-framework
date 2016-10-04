package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.meta.impl.MetaMappedSuperclassImpl;
import io.katharsis.jpa.model.TestMappedSuperclassWithPk;
import io.katharsis.jpa.model.TestSubclassWithSuperclassPk;

public class MetaEntityTest {

	@Test
	public void testPrimaryKeyOnParentMappedSuperClass() {
		MetaLookup lookup = new MetaLookup();
		MetaEntity meta = lookup.getMeta(TestSubclassWithSuperclassPk.class).asEntity();
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
	}

	@Test
	public void testPrimaryKeyOnMappedSuperClass() {
		MetaLookup lookup = new MetaLookup();
		MetaMappedSuperclassImpl meta = (MetaMappedSuperclassImpl) lookup.getMeta(TestMappedSuperclassWithPk.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
	}
}
