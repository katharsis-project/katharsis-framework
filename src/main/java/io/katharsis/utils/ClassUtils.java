package io.katharsis.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides reflection methods for parsing information about a class.
 */
public class ClassUtils {

    private static final ClassUtils INSTANCE = new ClassUtils();

    private ClassUtils() {
    }

    /**
     * Returns a list of class fields. Supports inheritance and doesn't return synthetic fields.
     *
     * @param beanClass class to be searched for
     * @return a list of found fields
     */
    public static List<Field> getClassFields(Class<?> beanClass) {
        Map<String, Field> result = new HashMap<>();

        Class<?> currentClass = beanClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (!field.isSynthetic()) {
                    result.putIfAbsent(field.getName(), field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return new LinkedList<>(result.values());
    }

    /**
     * <p>
     * Return a list of class getters. Supports inheritance and overriding, that is when a method is found on the
     * lowest level of inheritance chain, no other method can override it.
     * </p>
     * <p>
     * A getter:
     * <ul>
     * <li>Starts with an <i>is</i> if returns <i>boolean</i> or {@link Boolean} value</li>
     * <li>Starts with a <i>get</i> if returns non-boolean value</li>
     * </ul>
     * </p>
     *
     * @param beanClass class to be searched for
     * @return a list of found getters
     */
    public static List<Method> getClassGetters(Class<?> beanClass) {
        Map<String, Method> result = new HashMap<>();

        Class<?> currentClass = beanClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (INSTANCE.isGetter(method)) {
                    result.putIfAbsent(method.getName(), method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return new LinkedList<>(result.values());
    }

    /**
     * Return a list of class setters. Supports inheritance and overriding, that is when a method is found on the
     * lowest level of inheritance chain, no other method can override it.
     *
     * @param beanClass class to be searched for
     * @return a list of found getters
     */
    public static List<Method> getClassSetters(Class<?> beanClass) {
        Map<String, Method> result = new HashMap<>();

        Class<?> currentClass = beanClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (INSTANCE.isSetter(method)) {
                    result.putIfAbsent(method.getName(), method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return new LinkedList<>(result.values());
    }

    private boolean isGetter(Method method) {
        return isBooleanGetter(method) || isNonBooleanGetter(method);
    }

    private boolean isBooleanGetter(Method method) {
        if (!method.getName().startsWith("is"))
            return false;
        if (method.getName().length() < 3)
            return false;
        if (method.getParameterTypes().length != 0) return false;
        if (!(boolean.class.equals(method.getReturnType()) || Boolean.class.equals(method.getReturnType())))
            return false;
        return true;
    }

    private boolean isNonBooleanGetter(Method method) {
        if (!method.getName().startsWith("get"))
            return false;
        if (method.getName().length() < 4)
            return false;
        if (method.getParameterTypes().length != 0)
            return false;
        if (void.class.equals(method.getReturnType())) return false;
        return true;
    }

    private boolean isSetter(Method method) {
        if (!method.getName().startsWith("set"))
            return false;
        if (method.getName().length() < 4)
            return false;
        if (method.getParameterTypes().length != 1)
            return false;
        return true;
    }
}
