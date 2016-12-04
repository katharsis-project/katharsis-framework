package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.model.TestEntity;

public class MetaKeyImplTest {

	@Test
	public void test() {
		MetaLookup lookup = new MetaLookup();
		MetaEntity meta = lookup.getMeta(TestEntity.class).asEntity();
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertTrue(primaryKey.isUnique());
		Assert.assertEquals("_primaryKey", primaryKey.getName());
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals(Long.class, primaryKey.getType().getImplementationClass());
	}
}
