package io.katharsis.jpa.meta;

import java.io.Serializable;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import io.katharsis.meta.model.MetaPrimitiveType;

public class MetaPrimitiveTypeTest {

	@Test
	public void testString() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(String.class);
		Assert.assertEquals("test", type.fromString("test"));
	}

	@Test
	public void testInteger() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Integer.class);
		Assert.assertEquals(12, type.fromString("12"));
	}

	@Test
	public void testShort() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Short.class);
		Assert.assertEquals((short) 12, type.fromString("12"));
	}

	@Test
	public void testLong() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Long.class);
		Assert.assertEquals(12L, type.fromString("12"));
	}

	@Test
	public void testFloat() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Float.class);
		Assert.assertEquals(12.0f, type.fromString("12.0"));
	}

	@Test
	public void testDouble() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Double.class);
		Assert.assertEquals(12.0, type.fromString("12.0"));
	}

	@Test
	public void testBoolean() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Boolean.class);
		Assert.assertTrue((Boolean) type.fromString("true"));
	}

	@Test
	public void testByte() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Byte.class);
		Assert.assertEquals((byte) 12, type.fromString("12"));
	}

	@Test
	public void testUUID() {
		UUID uuid = UUID.randomUUID();
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(UUID.class);
		Assert.assertEquals(uuid, type.fromString(uuid.toString()));
	}

	enum TestEnum {
		A
	}

	@Test
	public void testEnum() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(TestEnum.class);
		Assert.assertEquals(TestEnum.A, type.fromString("A"));
	}

	public static class TestObjectWithParse {

		int value;

		public static TestObjectWithParse parse(String value) {
			TestObjectWithParse parser = new TestObjectWithParse();
			parser.value = Integer.parseInt(value);
			return parser;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestObjectWithParse other = (TestObjectWithParse) obj;
			if (value != other.value)
				return false;
			return true;
		}
	}

	public static class TestObjectWithConstructor implements Serializable {

		int value;

		public TestObjectWithConstructor() {
		}

		public TestObjectWithConstructor(String value) {
			this.value = Integer.parseInt(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestObjectWithConstructor other = (TestObjectWithConstructor) obj;
			if (value != other.value)
				return false;
			return true;
		}
	}

	@Test
	public void testParse() {
		TestObjectWithParse value = new TestObjectWithParse();
		value.value = 12;
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(TestObjectWithParse.class);
		Assert.assertEquals(value, type.fromString("12"));
	}

	@Test
	public void testOther() {
		TestObjectWithConstructor value = new TestObjectWithConstructor();
		value.value = 12;
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(TestObjectWithConstructor.class);
		Assert.assertEquals(value, type.fromString("12"));
	}

}
