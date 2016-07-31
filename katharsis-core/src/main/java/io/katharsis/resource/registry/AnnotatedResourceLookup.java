package io.katharsis.resource.registry;

import io.katharsis.dispatcher.registry.ResourceRegistry;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.resource.annotations.JsonApiResource;
import lombok.Value;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Set;

/**
 * Scans only for annotated {@link JsonApiResource} and {@link JsonApiResourceRepository}
 */
@Value
public class AnnotatedResourceLookup implements ResourceLookup {

    private String packageName;
    private Reflections reflections;

    public AnnotatedResourceLookup(String packageName) {
        this.packageName = packageName;
        if (packageName != null) {
            String[] packageNames = packageName.split(",");
            reflections = new Reflections(packageNames);
        } else {
            reflections = new Reflections(packageName);
        }
    }

    @Override
    public ResourceRegistry scan(String[] packages) {
        return null;
    }

    @Override
    public Set<Class<?>> getResourceClasses() {
        return reflections.getTypesAnnotatedWith(JsonApiResource.class);
    }

    @Override
    public Set<Class<?>> getResourceRepositoryClasses() {
        final Set<Class<?>> result = new HashSet<>();

        Set<Class<?>> annotatedResourceRepositories = reflections.getTypesAnnotatedWith(JsonApiResourceRepository.class);
        Set<Class<?>> annotatedRelationshipRepositories = reflections.getTypesAnnotatedWith(JsonApiRelationshipRepository.class);

        result.addAll(annotatedResourceRepositories);
        result.addAll(annotatedRelationshipRepositories);
        return result;
    }
}
