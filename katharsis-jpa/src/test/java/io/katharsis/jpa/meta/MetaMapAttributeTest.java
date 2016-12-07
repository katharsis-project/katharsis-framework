package io.katharsis.jpa.meta;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaMapType;
import io.katharsis.jpa.internal.meta.impl.MetaDataObjectImpl;
import io.katharsis.jpa.internal.meta.impl.MetaMapAttributeImpl;

public class MetaMapAttributeTest {

	private MetaMapAttributeImpl impl;

	private MetaAttribute mapAttr;

	private MetaMapType mapType;

	private MetaDataObjectImpl parent;

	@Before
	public void setup() {
		String keyString = "test";
		mapAttr = Mockito.mock(MetaAttribute.class);
		mapType = Mockito.mock(MetaMapType.class);
		impl = new MetaMapAttributeImpl(mapType, mapAttr, keyString);
		parent = Mockito.mock(MetaDataObjectImpl.class);
		impl.setParent(parent);
	}

	@Test
	public void testGetters() {
		Assert.assertEquals(mapAttr, impl.getMapAttribute());
		Assert.assertEquals(parent, impl.getParent());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getIdNotSupported() {
		impl.getId();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getVersionNotSupported() {
		impl.isVersion();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void isIdNotSupported() {
		impl.isId();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getOppositeAttributeNotSupported() {
		impl.getOppositeAttribute();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getValueNotSupported() {
		impl.getValue(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void addValueNotSupported() {
		impl.addValue(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void removeValueNotSupported() {
		impl.removeValue(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setValueNotSupported() {
		impl.setValue(null, null);
	}
}
