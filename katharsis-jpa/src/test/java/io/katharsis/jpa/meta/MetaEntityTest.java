package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Test;

//import io.katharsis.jpa.model.SequenceEntity;
import io.katharsis.jpa.model.TestMappedSuperclassWithPk;
import io.katharsis.jpa.model.TestSubclassWithSuperclassPk;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaDataObject;
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

	// FIXME
//	@Test
//	public void testGeneratedPrimaryKey() {
//		MetaLookup lookup = new MetaLookup();
//		lookup.addProvider(new JpaMetaProvider());
//		MetaDataObject meta = lookup.getMeta(SequenceEntity.class).asDataObject();
//		MetaPrimaryKey primaryKey = meta.getPrimaryKey();
//		Assert.assertNotNull(primaryKey);
//		Assert.assertEquals(1, primaryKey.getElements().size());
//		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
//		Assert.assertTrue(primaryKey.isGenerated());
//	}
}
