package io.katharsis.resource.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import io.katharsis.utils.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Returns a name of a field. It takes into account {@link JsonProperty} annotation and {@link PropertyNamingStrategy}
 * can also be used. Note that {@link JsonProperty} overrides {@link PropertyNamingStrategy}.
 */
public class ResourceFieldNameTransformer {
    private final SerializationConfig serializationConfig;

    public ResourceFieldNameTransformer() {
        this(null);
    }

    public ResourceFieldNameTransformer(SerializationConfig serializationConfig) {
        this.serializationConfig = serializationConfig;
    }

    public String getName(Field field) {

        String name = field.getName();
        if (field.isAnnotationPresent(JsonProperty.class) &&
            !"".equals(field.getAnnotation(JsonProperty.class).value())) {
            name = field.getAnnotation(JsonProperty.class).value();
        } else if (serializationConfig != null && serializationConfig.getPropertyNamingStrategy() != null) {
            AnnotationMap annotationMap = buildAnnotationMap(field.getDeclaredAnnotations());

            AnnotatedClass annotatedClass = AnnotatedClassBuilder.build(field.getDeclaringClass(), serializationConfig);
            AnnotatedField annotatedField = new AnnotatedField(annotatedClass, field, annotationMap);
            name = serializationConfig.getPropertyNamingStrategy().nameForField(serializationConfig, annotatedField, name);
        }
        return name;
    }

    /**
     * Extract name to be used by Katharsis from getter's name. It uses
     * {@link ResourceFieldNameTransformer#getMethodName(Method)}, {@link JsonProperty} annotation and
     * {@link PropertyNamingStrategy}.
     *
     * @param method method to extract name
     * @return method name
     */
    public String getName(Method method) {
        String name = getMethodName(method);

        if (method.isAnnotationPresent(JsonProperty.class) &&
            !"".equals(method.getAnnotation(JsonProperty.class).value())) {
            name = method.getAnnotation(JsonProperty.class).value();
        } else if (serializationConfig != null && serializationConfig.getPropertyNamingStrategy() != null) {
            Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
            AnnotationMap annotationMap = buildAnnotationMap(declaredAnnotations);

            int paramsLength = method.getParameterAnnotations().length;
            AnnotationMap[] paramAnnotations = new AnnotationMap[paramsLength];
            for (int i = 0; i < paramsLength; i++) {
                AnnotationMap parameterAnnotationMap = buildAnnotationMap(method.getParameterAnnotations()[i]);
                paramAnnotations[i] = parameterAnnotationMap;
            }

            AnnotatedClass annotatedClass = AnnotatedClassBuilder.build(method.getDeclaringClass(), serializationConfig);
            AnnotatedMethod annotatedField = new AnnotatedMethod(annotatedClass, method, annotationMap, paramAnnotations);
            name = serializationConfig.getPropertyNamingStrategy().nameForGetterMethod(serializationConfig, annotatedField, name);
        }
        return name;
    }

    private static AnnotationMap buildAnnotationMap(Annotation[] declaredAnnotations) {
        AnnotationMap annotationMap = new AnnotationMap();
        for (Annotation annotation : declaredAnnotations) {
            annotationMap.add(annotation);
        }
        return annotationMap;
    }

    /**
     * Extract Java bean name from getter's name
     *
     * @param method method to extract name
     * @return extracted method name
     */
    public String getMethodName(Method method) {
        String name;
        if (ClassUtils.isBooleanGetter(method)) {
            name = extractMethodName(method, 2);
        } else {
            name = extractMethodName(method, 3);
        }
        return name;
    }

    private static String extractMethodName(Method method, int nameStart) {
        String resourceName = method.getName().substring(nameStart);
        return resourceName.substring(0, 1).toLowerCase() + resourceName.substring(1);
    }
}
