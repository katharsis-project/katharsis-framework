package io.katharsis.utils;

import java.lang.reflect.Field;

/**
 * Bean utils based on Katharsis PropertyUtils
 */
public class BeanUtils {

    /**
     * Get bean's property value and maps to String
     *
     * @param bean  bean to be accessed
     * @param field bean's field
     * @return bean's property value
     */
    public static String getProperty(Object bean, Field field) {
        Object property = PropertyUtils.getProperty(bean, field);
        if (property == null) {
            return "null";
        }

        return property.toString();
    }
}
