package io.katharsis.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * <p>
 * A lighter version of Apache Commons PropertyUtils without additional dependencies and with support for fluent
 * setters and {@link JsonProperty} annotation.
 * </p>
 */
public class PropertyUtils {

    private static final PropertyUtils INSTANCE = new PropertyUtils();

    private PropertyUtils() {
    }

    /**
     * Get bean's property value
     *
     * @param bean  bean to be accessed
     * @param field bean's fieldName
     * @return bean's property value
     */
    public static Object getProperty(Object bean, String field) {
        INSTANCE.checkParameters(bean, field);

        try {
            return INSTANCE.getPropertyValue(bean, field);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkParameters(Object bean, String field) {
        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        if (field == null) {
            throw new IllegalArgumentException(String.format("No field specified for bean: %s", bean.getClass()));
        }
    }

    private Object getPropertyValue(Object bean, String fieldName)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        Field foundField = findField(bean, fieldName);
        if (foundField != null) {
            if (Modifier.isPrivate(foundField.getModifiers())) {
                Method getter = getGetter(bean, foundField);
                return getter.invoke(bean);
            } else if (Modifier.isPublic(foundField.getModifiers())) {
                return foundField.get(bean);
            } else {
                throw new RuntimeException(
                    String.format("Couldn't access a field %s.%s", bean.getClass().getCanonicalName(), fieldName));
            }
        }
        throw new RuntimeException(
            String.format("Couldn't find a field %s.%s", bean.getClass().getCanonicalName(), fieldName));
    }

    private Field findField(Object bean, String fieldName) {
        Field foundField = null;
        List<Field> classFields = ClassUtils.getClassFields(bean.getClass());
        for (Field field : classFields) { // The first loop tries to get name from annotation
            if (field.isAnnotationPresent(JsonProperty.class)
                && fieldName.equals(field.getAnnotation(JsonProperty.class).value())) {
                foundField = field;
                break;
            }
        }
        for (Field field : classFields) { // The second just tries to get by internal name
            if (field.getName().equals(fieldName)) {
                foundField = field;
                break;
            }
        }
        return foundField;
    }

    private Method getGetter(Object bean, Field field) throws NoSuchMethodException {
        Class<?> beanClass = bean.getClass();

        String name = field.getName();
        String upperCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);

        try {
            return beanClass.getMethod("get" + upperCaseName);
        } catch (NoSuchMethodException e) {
            return beanClass.getMethod("is" + upperCaseName);
        }
    }

    public static void setProperty(Object bean, String field, Object value) {
        INSTANCE.checkParameters(bean, field);

        try {
            INSTANCE.setPropertyValue(bean, field, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void setPropertyValue(Object bean, String fieldName, Object value)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Field foundField = findField(bean, fieldName);

        if (fieldName != null) {
            if (Modifier.isPrivate(foundField.getModifiers())) {
                Method setter = getSetter(bean, foundField);
                setter.invoke(bean, value);
            } else if (Modifier.isPublic(foundField.getModifiers())) {
                foundField.set(bean, value);
            } else {
                throw new RuntimeException(
                    String.format("Couldn't access a field %s.%s", bean.getClass().getCanonicalName(), fieldName));
            }
        } else {
            throw new RuntimeException(
                String.format("Couldn't find a field %s.%s", bean.getClass().getCanonicalName(), fieldName));
        }
    }

    private Method getSetter(Object bean, Field field) throws NoSuchMethodException {
        Class<?> beanClass = bean.getClass();

        String name = field.getName();
        String upperCaseName = name.substring(0, 1).toUpperCase() + name.substring(1);

        return beanClass.getMethod("set" + upperCaseName, field.getType());
    }
}
