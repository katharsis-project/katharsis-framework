package io.katharsis.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ClassUtils {

    private static final ClassUtils INSTANCE = new ClassUtils();

    private ClassUtils() {
    }

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

    private boolean isGetter(Method method) {
        if (!method.getName().startsWith("get")) return false;
        if (method.getParameterTypes().length != 0) return false;
        if (void.class.equals(method.getReturnType())) return false;
        return true;
    }

}
