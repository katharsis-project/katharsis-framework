package io.katharsis.legacy.registry;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.katharsis.core.internal.utils.Predicate1;
import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.legacy.repository.annotations.JsonApiResourceRepository;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.registry.ResourceEntry;
import io.katharsis.resource.registry.ResourceLookup;
import io.katharsis.resource.registry.ResponseRelationshipEntry;

/**
 * Repository entries builder for classes annotated with repository annotations.
 */
public class AnnotatedRepositoryEntryBuilder implements RepositoryEntryBuilder {

    private final JsonServiceLocator jsonServiceLocator;
    
	private ModuleRegistry moduleRegistry;

    public AnnotatedRepositoryEntryBuilder(ModuleRegistry moduleRegistry, JsonServiceLocator jsonServiceLocator) {
    	this.moduleRegistry = moduleRegistry;
        this.jsonServiceLocator = jsonServiceLocator;
    }

    @Override
    public ResourceEntry buildResourceRepository(ResourceLookup lookup, final Class<?> resourceClass) {
        Predicate1<Class<?>> classPredicate = new Predicate1<Class<?>>() {
            @Override
            public boolean test(Class<?> clazz) {
                return resourceClass.equals(clazz.getAnnotation(JsonApiResourceRepository.class).value());
            }
        };

        List<Class<?>> repositoryClasses = findRepositoryClasses(lookup, classPredicate, JsonApiResourceRepository.class);
        if (repositoryClasses.isEmpty()) {
            return null;
        } else {
            return new AnnotatedResourceEntry<>(moduleRegistry, new RepositoryInstanceBuilder<>(jsonServiceLocator, repositoryClasses.get(0)));
        }
    }

    @Override
    public List<ResponseRelationshipEntry> buildRelationshipRepositories(ResourceLookup lookup, final Class<?> resourceClass) {
        Predicate1<Class<?>> classPredicate = new Predicate1<Class<?>>() {
            @Override
            public boolean test(Class<?> clazz) {
                JsonApiRelationshipRepository annotation = clazz.getAnnotation(JsonApiRelationshipRepository.class);
                return resourceClass.equals(annotation.source());
            }
        };

        List<Class<?>> repositoryClasses = findRepositoryClasses(lookup, classPredicate, JsonApiRelationshipRepository.class);
        List<ResponseRelationshipEntry> relationshipEntries = new ArrayList<>(repositoryClasses.size());
        for (Class<?> repositoryClass : repositoryClasses) {
            relationshipEntries.add(new AnnotatedRelationshipEntryBuilder(moduleRegistry, new RepositoryInstanceBuilder<>(jsonServiceLocator, repositoryClass)));
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
