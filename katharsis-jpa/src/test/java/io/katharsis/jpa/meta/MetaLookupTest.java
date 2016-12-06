package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.jpa.internal.meta.MetaArrayType;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.jpa.internal.meta.impl.MetaPrimitiveType;
import io.katharsis.jpa.model.TestEntity;

public class MetaLookupTest {

	@Test
	public void testObjectArrayMeta() {
		MetaLookup lookup = new MetaLookup();

		MetaArrayType meta = (MetaArrayType) lookup.getMeta(TestEntity[].class).asType();
		MetaType elementType = meta.getElementType();
		Assert.assertTrue(elementType instanceof MetaEntity);
	}

	@Test
	public void testPrimitiveArrayMeta() {
		MetaLookup lookup = new MetaLookup();

		MetaPrimitiveType type = (MetaPrimitiveType) lookup.getMeta(byte[].class).asType();
		Assert.assertEquals(byte[].class, type.getImplementationClass());
	}

}
