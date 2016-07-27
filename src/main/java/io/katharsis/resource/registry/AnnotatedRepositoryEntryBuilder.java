package io.katharsis.resource.registry;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.repository.RepositoryInstanceBuilder;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.resource.registry.repository.AnnotatedRelationshipEntryBuilder;
import io.katharsis.resource.registry.repository.AnnotatedResourceEntryBuilder;
import io.katharsis.resource.registry.repository.ResourceEntry;
import io.katharsis.resource.registry.repository.ResponseRelationshipEntry;
import io.katharsis.utils.Predicate1;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Repository entries builder for classes annotated with repository annotations.
 */
public class AnnotatedRepositoryEntryBuilder implements RepositoryEntryBuilder {

    private final JsonServiceLocator jsonServiceLocator;

    public AnnotatedRepositoryEntryBuilder(JsonServiceLocator jsonServiceLocator) {
        this.jsonServiceLocator = jsonServiceLocator;
    }

    @Override
    public ResourceEntry<?, ?> buildResourceRepository(ResourceLookup lookup, final Class<?> resourceClass) {
        Predicate1<Class<?>> classPredicate = new Predicate1<Class<?>>() {
            @Override
            public boolean test(Class<?> clazz) {
                return resourceClass.equals(clazz.getAnnotation(JsonApiResourceRepository.class).value());
            }
        };

        List<Class<?>> repositoryClasses = findRepositoryClasses(lookup, classPredicate, JsonApiResourceRepository.class);
        if (repositoryClasses.size() == 0) {
            return null;
        } else {
            return new AnnotatedResourceEntryBuilder<>(new RepositoryInstanceBuilder<>(jsonServiceLocator, repositoryClasses.get(0)));
        }
    }

    @Override
    public List<ResponseRelationshipEntry<?, ?>> buildRelationshipRepositories(ResourceLookup lookup, final Class<?> resourceClass) {
        Predicate1<Class<?>> classPredicate = new Predicate1<Class<?>>() {
            @Override
            public boolean test(Class<?> clazz) {
                JsonApiRelationshipRepository annotation = clazz.getAnnotation(JsonApiRelationshipRepository.class);
                return resourceClass.equals(annotation.source());
            }
        };

        List<Class<?>> repositoryClasses = findRepositoryClasses(lookup, classPredicate, JsonApiRelationshipRepository.class);
        List<ResponseRelationshipEntry<?, ?>> relationshipEntries = new ArrayList<>(repositoryClasses.size());
        for (Class<?> repositoryClass : repositoryClasses) {
            relationshipEntries.add(new AnnotatedRelationshipEntryBuilder<>(new RepositoryInstanceBuilder<>(jsonServiceLocator, repositoryClass)));
        }

        return relationshipEntries;
    }

    private List<Class<?>> findRepositoryClasses(ResourceLookup lookup, Predicate1<Class<?>> classPredicate, Class<? extends Annotation> annotation) {
        List<Class<?>> repositoryClasses = new LinkedList<>();

        for (Class<?> clazz : lookup.getResourceRepositoryClasses()) {
            if (clazz.isAnnotationPresent(annotation) && classPredicate.test(clazz)) {
                repositoryClasses.add(clazz);
            }
        }
        return repositoryClasses;
    }
}
