package io.katharsis.utils;

import java.lang.reflect.*;

public class Generics {

    public static Class<?> getResourceClass(Field relationshipField, Class baseClass) {
        if (Iterable.class.isAssignableFrom(baseClass)) {
            Type genericFieldType = relationshipField.getGenericType();
            if (genericFieldType instanceof ParameterizedType) {
                ParameterizedType aType = (ParameterizedType) genericFieldType;
                Type[] fieldArgTypes = aType.getActualTypeArguments();
                if (fieldArgTypes.length == 1 && fieldArgTypes[0] instanceof Class<?>) {
                    return (Class) fieldArgTypes[0];
                } else {
                    throw new RuntimeException("Wrong type: " + aType);
                }
            } else {
                throw new RuntimeException("The relationship must be parametrized (cannot be wildcard or array): "
                        + genericFieldType);
            }
        }
        return baseClass;
    }
}
