package io.katharsis.meta.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import io.katharsis.core.internal.utils.parser.TypeParser;
import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "meta/primitiveType")
public class MetaPrimitiveType extends MetaType {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object fromString(String value) { // NOSONAR

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

		if (implClass == UUID.class)
			return UUID.fromString(value);

		Object result = checkParse(implClass, value);
		if (result != null) {
			return result;
		}

		TypeParser typeParser = new TypeParser();
		return typeParser.parse(value, (Class) implClass);
	}

	private Object checkParse(Class<?> implClass, String value) {
		Method method;
		try {
			method = implClass.getMethod("parse", String.class);
			return method.invoke(implClass, value);
		}
		catch (NoSuchMethodException e) { // NOSONAR
			// not available
			return null;
		}
		catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}
}
