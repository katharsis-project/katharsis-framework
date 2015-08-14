package io.katharsis.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * <p>
 * A lighter version of Apache Commons PropertyUtils without additional dependencies and with support for fluent
 * setters.
 * </p>
 */
public class PropertyUtils {

    /**
     * Get bean's property value
     *
     * @param bean  bean to be accessed
     * @param field bean's field
     * @return bean's property value
     */
    public static Object getProperty(Object bean, Field field) {
        checkParameters(bean, field);

        try {
            return getPropertyValue(bean, field);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkParameters(Object bean, Field field) {
        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        if (field == null) {
            throw new IllegalArgumentException(String.format("No field specified for bean: %s", bean.getClass()));
        }
    }

    private static Object getPropertyValue(Object bean, Field field)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (Modifier.isPrivate(field.getModifiers())) {
            Method getter = getGetter(bean, field);
            return getter.invoke(bean);
        } else if (Modifier.isPublic(field.getModifiers())) {
            return field.get(bean);
        } else {
            throw new RuntimeException(String.format("Couldn't access a field %s.%s",
                bean.getClass().getCanonicalName(), field.getName()));
        }
    }

    private static Method getGetter(Object bean, Field field) throws NoSuchMethodException {
        Class<?> beanClass = bean.getClass();

        String name = field.getName();
        String upperCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);

        try {
            return beanClass.getMethod("get" + upperCaseName);
        } catch (NoSuchMethodException e) {
            return beanClass.getMethod("is" + upperCaseName);
        }
    }

    public static void setProperty(Object bean, Field field, Object value) {
        checkParameters(bean, field);

        try {
            setPropertyValue(bean, field, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setPropertyValue(Object bean, Field field, Object value)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (Modifier.isPrivate(field.getModifiers())) {
            Method setter = getSetter(bean, field);
            setter.invoke(bean, value);
        } else if (Modifier.isPublic(field.getModifiers())) {
            field.set(bean, value);
        } else {
            throw new RuntimeException(String.format("Couldn't access a field %s.%s",
                bean.getClass().getCanonicalName(), field.getName()));
        }
    }

    private static Method getSetter(Object bean, Field field) throws NoSuchMethodException {
        Class<?> beanClass = bean.getClass();

        String name = field.getName();
        String upperCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);

        return beanClass.getMethod("set" + upperCaseName, field.getType());
    }
}
