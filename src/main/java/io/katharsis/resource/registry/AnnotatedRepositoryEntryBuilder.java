package io.katharsis.resource.registry;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.repository.exception.RepositoryInstanceNotFoundException;
import io.katharsis.resource.registry.repository.AnnotatedRelationshipEntryBuilder;
import io.katharsis.resource.registry.repository.AnnotatedResourceEntryBuilder;
import io.katharsis.resource.registry.repository.RelationshipEntry;
import io.katharsis.resource.registry.repository.ResourceEntry;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Repository entries builder for classes annotated with repository annotations.
 */
public class AnnotatedRepositoryEntryBuilder implements RepositoryEntryBuilder {

    private final JsonServiceLocator jsonServiceLocator;

    public AnnotatedRepositoryEntryBuilder(JsonServiceLocator jsonServiceLocator) {
        this.jsonServiceLocator = jsonServiceLocator;
    }

    @Override
    public ResourceEntry<?, ?> buildResourceRepository(Reflections reflections, Class<?> resourceClass) {
        Predicate<Class<?>> classPredicate =
            clazz -> resourceClass.equals(clazz.getAnnotation(JsonApiResourceRepository.class).value());

        List<Object> repositoryObjects = findRepositoryObject(reflections, classPredicate, JsonApiResourceRepository.class);
        if (repositoryObjects.size() == 0) {
            return null;
        } else {
            return new AnnotatedResourceEntryBuilder<>(repositoryObjects.get(0));
        }
    }

    @Override
    public List<RelationshipEntry<?, ?>> buildRelationshipRepositories(Reflections reflections, Class<?> resourceClass) {
        Predicate<Class<?>> classPredicate =
            clazz -> resourceClass.equals(clazz.getAnnotation(JsonApiRelationshipRepository.class).source());

        List<Object> repositoryObjects = findRepositoryObject(reflections, classPredicate, JsonApiRelationshipRepository.class);
        return repositoryObjects.stream()
            .map(AnnotatedRelationshipEntryBuilder::new)
            .collect(Collectors.toList());
    }

    private List<Object> findRepositoryObject(Reflections reflections, Predicate<Class<?>> classPredicate, Class<? extends Annotation> annotation) {
        return reflections.getTypesAnnotatedWith(annotation).stream()
            .filter(classPredicate)
            .map(clazz -> {
                Object instance = jsonServiceLocator.getInstance(clazz);
                if (instance == null) {
                    throw new RepositoryInstanceNotFoundException(clazz.getCanonicalName());
                }
                return instance;
            })
            .collect(Collectors.toList());
    }
}
