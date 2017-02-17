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
	}

	@Test
	public void testInteger() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Integer.class);
	}

	@Test
	public void testShort() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Short.class);
	}

	@Test
	public void testLong() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Long.class);
	}

	@Test
	public void testFloat() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Float.class);
	}

	@Test
	public void testDouble() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Double.class);
	}

	@Test
	public void testBoolean() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Boolean.class);
	}

	@Test
	public void testByte() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(Byte.class);
	}

	@Test
	public void testUUID() {
		UUID uuid = UUID.randomUUID();
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(UUID.class);
	}

	enum TestEnum {
		A
	}

	@Test
	public void testEnum() {
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(TestEnum.class);
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
	}

	@Test
	public void testOther() {
		TestObjectWithConstructor value = new TestObjectWithConstructor();
		value.value = 12;
		MetaPrimitiveType type = new MetaPrimitiveType();
		type.setImplementationType(TestObjectWithConstructor.class);
	}

}
