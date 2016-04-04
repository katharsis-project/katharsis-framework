package io.katharsis.resource.registry;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.repository.exception.RepositoryInstanceNotFoundException;
import io.katharsis.resource.registry.repository.AnnotatedRelationshipEntryBuilder;
import io.katharsis.resource.registry.repository.AnnotatedResourceEntryBuilder;
import io.katharsis.resource.registry.repository.RelationshipEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;
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

        List<Object> repositoryObjects = findRepositoryObject(lookup, classPredicate, JsonApiResourceRepository.class);
        if (repositoryObjects.size() == 0) {
            return null;
        } else {
            return new AnnotatedResourceEntryBuilder<>(repositoryObjects.get(0));
        }
    }

    @Override
    public List<RelationshipEntry<?, ?>> buildRelationshipRepositories(ResourceLookup lookup, final Class<?> resourceClass) {
        Predicate1<Class<?>> classPredicate = new Predicate1<Class<?>>() {
            @Override
            public boolean test(Class<?> clazz) {
                JsonApiRelationshipRepository annotation = clazz.getAnnotation(JsonApiRelationshipRepository.class);
                return resourceClass.equals(annotation.source());
            }
        };

        List<Object> repositoryObjects = findRepositoryObject(lookup, classPredicate, JsonApiRelationshipRepository.class);
        List<RelationshipEntry<?, ?>> relationshipEntries = new ArrayList<>(repositoryObjects.size());
        for (Object repositoryObject : repositoryObjects) {
            relationshipEntries.add(new AnnotatedRelationshipEntryBuilder<>(repositoryObject));
        }

        return relationshipEntries;
    }

    private List<Object> findRepositoryObject(ResourceLookup lookup, Predicate1<Class<?>> classPredicate, Class<? extends Annotation> annotation) {
        List<Object> repositoryObjects = new LinkedList<>();

        for (Class<?> clazz : lookup.getResourceRepositoryClasses()) {
            if (clazz.isAnnotationPresent(annotation) && classPredicate.test(clazz)) {
                Object instance = jsonServiceLocator.getInstance(clazz);
                if (instance == null) {
                    throw new RepositoryInstanceNotFoundException(clazz.getCanonicalName());
                }
                repositoryObjects.add(instance);
            }
        }
        return repositoryObjects;
    }
}
