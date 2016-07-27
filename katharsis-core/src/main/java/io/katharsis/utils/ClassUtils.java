package io.katharsis.utils;

import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.utils.java.Optional;

import java.lang.annotation.Annotation;
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
                    Field v = result.get(field.getName());
                    if (v == null) {
                        result.put(field.getName(), field);
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return new LinkedList<>(result.values());
    }

    /**
     * Returns an instance of bean's annotation
     *
     * @param beanClass       class to be searched for
     * @param annotationClass type of an annotation
     * @param <T>             type of an annotation
     * @return an instance of an annotation
     */
    public static <T extends Annotation> Optional<T> getAnnotation(Class<?> beanClass, Class<T> annotationClass) {
        Class<?> currentClass = beanClass;
        while (currentClass != null && currentClass != Object.class) {
            if (currentClass.isAnnotationPresent(annotationClass)) {
                return Optional.of(currentClass.getAnnotation(annotationClass));
            }
            currentClass = currentClass.getSuperclass();
        }

        return Optional.empty();
    }

    /**
     * Tries to find a class fields. Supports inheritance and doesn't return synthetic fields.
     *
     * @param beanClass class to be searched for
     * @param fieldName field name
     * @return a list of found fields
     */
    public static Field findClassField(Class<?> beanClass, String fieldName) {
        Class<?> currentClass = beanClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isSynthetic()) {
                    continue;
                }

                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return null;
    }

    /**
     * <p>
     * Return a list of class getters. Supports inheritance and overriding, that is when a method is found on the
     * lowest level of inheritance chain, no other method can override it.
     * <p>
     * A getter:
     * <ul>
     *   <li>Starts with an <i>is</i> if returns <i>boolean</i> or {@link Boolean} value</li>
     *   <li>Starts with a <i>get</i> if returns non-boolean value</li>
     * </ul>
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
                    Method v = result.get(method.getName());
                    if (v == null) {
                        result.put(method.getName(), method);
                    }
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
                    Method v = result.get(method.getName());
                    if (v == null) {
                        result.put(method.getName(), method);
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return new LinkedList<>(result.values());
    }

    /**
     * Return a first occurrence of a method annotated with specified annotation
     *
     * @param searchClass    class to be searched
     * @param annotationClass annotation class
     * @return annotated method or null
     */
    public static Method findMethodWith(Class<?> searchClass, Class<? extends Annotation> annotationClass) {
        Method foundMethod = null;
        methodFinder:
        while (searchClass != null && searchClass != Object.class) {
            for (Method method : searchClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationClass)) {
                    foundMethod = method;
                    break methodFinder;
                }
            }
            searchClass = searchClass.getSuperclass();
        }

        return foundMethod;
    }

    /**
     * Returns the first clazz in the ancestor hierarchy with the JsonApiResource annotation
     *
     * @param data instance
     * @param <T>  instance type
     * @return class or null
     */
    public static <T> Class<? super T> getJsonApiResourceClass(final T data) {
        return getJsonApiResourceClass((Class<? super T>) data.getClass());
    }

    public static <T> Class<? super T> getJsonApiResourceClass(final Class<T> candidateClass) {
        Class<? super T> currentClass = candidateClass;
        while (currentClass != null && currentClass != Object.class) {
            if (currentClass.isAnnotationPresent(JsonApiResource.class)) {
                return currentClass;
            }
            currentClass = currentClass.getSuperclass();
        }

        return null;
    }

    /**
     * Create a new instance of a resource using a default constructor
     *
     * @param clazz new instance class
     * @param <T> new instance class
     * @return new instance
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ResourceException(String.format("couldn't create a new instance of %s", clazz));
        }
    }

    private boolean isGetter(Method method) {
        return isBooleanGetter(method) || isNonBooleanGetter(method);
    }

    public static boolean isBooleanGetter(Method method) {
        if (!method.getName().startsWith("is"))
            return false;
        if (method.getName().length() < 3)
            return false;
        if (method.getParameterTypes().length != 0) {
            return false;
        }
        return boolean.class.equals(method.getReturnType()) || Boolean.class.equals(method.getReturnType());
    }

    private boolean isNonBooleanGetter(Method method) {
        if (!method.getName().startsWith("get"))
            return false;
        if (method.getName().length() < 4)
            return false;
        if (method.getParameterTypes().length != 0)
            return false;
        return !void.class.equals(method.getReturnType());
    }

    private static boolean isSetter(Method method) {
        if (!method.getName().startsWith("set"))
            return false;
        if (method.getName().length() < 4)
            return false;
        return method.getParameterTypes().length == 1;
    }
}
