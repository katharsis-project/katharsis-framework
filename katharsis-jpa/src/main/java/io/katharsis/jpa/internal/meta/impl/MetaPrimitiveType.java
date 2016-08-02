package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

public class MetaPrimitiveType extends MetaTypeImpl {

	public MetaPrimitiveType(Class<?> implClass, Type implType) {
		super(null, implClass, implType);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object fromString(String value) {

		Class<?> implClass = getImplementationClass();
		if (implClass == String.class)
			return value;

		if (Number.class.isAssignableFrom(implClass) || implClass.isPrimitive()) {
			if (implClass == long.class || implClass == Long.class)
				return Long.valueOf(value);
			if (implClass == int.class || implClass == Integer.class)
				return Integer.valueOf(value);
			if (implClass == float.class || implClass == Float.class)
				return Float.valueOf(value);
			if (implClass == double.class || implClass == Double.class)
				return Double.valueOf(value);
			if (implClass == byte.class || implClass == Byte.class)
				return Byte.valueOf(value);
		}

		if (implClass == boolean.class || implClass == Boolean.class)
			return Boolean.valueOf(value);

		if (implClass == OffsetDateTime.class)
			return OffsetDateTime.parse(value);
		if (implClass == OffsetTime.class)
			return OffsetTime.parse(value);
		if (implClass == LocalTime.class)
			return LocalTime.parse(value);
		if (implClass == LocalDateTime.class)
			return LocalDateTime.parse(value);
		if (implClass == LocalDate.class)
			return LocalDate.parse(value);

		if (Enum.class.isAssignableFrom(implClass))
			return Enum.valueOf((Class) implClass, value);

		throw new UnsupportedOperationException("cannot parse \"" + value + "\" of type " + implClass.getName());
	}
}
