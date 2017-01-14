package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.model.TestEntity;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaKey;

public class MetaKeyImplTest {

	@Test
	public void test() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		MetaEntity meta = lookup.getMeta(TestEntity.class, MetaEntity.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertTrue(primaryKey.isUnique());
		Assert.assertEquals("TestEntity$primaryKey", primaryKey.getName());
		Assert.assertEquals(1, primaryKey.getElements().size());
	}
}
