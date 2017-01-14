package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.model.TestEntity;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaArrayType;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaPrimitiveType;
import io.katharsis.meta.model.MetaType;

public class MetaLookupTest {

	@Test
	public void testObjectArrayMeta() {
		MetaLookup lookup = new MetaLookup();
		lookup.addProvider(new JpaMetaProvider());
		
		MetaArrayType meta = lookup.getArrayMeta(TestEntity[].class, MetaEntity.class);
		MetaType elementType = meta.getElementType();
		Assert.assertTrue(elementType instanceof MetaDataObject);
	}

	@Test
	public void testPrimitiveArrayMeta() {
		MetaLookup lookup = new MetaLookup();

		MetaPrimitiveType type = (MetaPrimitiveType) lookup.getMeta(byte[].class).asType();
		Assert.assertEquals(byte[].class, type.getImplementationClass());
	}

}
