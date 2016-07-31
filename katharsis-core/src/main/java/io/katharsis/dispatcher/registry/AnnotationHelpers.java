package io.katharsis.dispatcher.registry;

import io.katharsis.errorhandling.exception.KatharsisInitializationException;
import io.katharsis.resource.annotations.JsonApiResource;
import lombok.NonNull;

import java.lang.annotation.Annotation;

public class AnnotationHelpers {

    public static String getResourceType(@NonNull Object instance) {
        // simple implementation
        return getResourceType(instance.getClass());
    }

    public static String getResourceType(@NonNull Class resourceClass) {
        JsonApiResource annotation = getAnnotation(resourceClass, JsonApiResource.class);
        return annotation.type();
    }

    public static <T extends Annotation> T getAnnotation(@NonNull Class classWithAnnotation,
                                                         @NonNull Class annotationToLookFor) {
        Annotation annotation = classWithAnnotation.getAnnotation(annotationToLookFor);
        if (annotation == null) {
            throw new KatharsisInitializationException(String
                    .format("Required annotation %s is missing from %s", annotationToLookFor, classWithAnnotation));
        }
        return (T) annotation;
    }

}
