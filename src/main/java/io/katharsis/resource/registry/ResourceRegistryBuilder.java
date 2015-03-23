package io.katharsis.resource.registry;

import io.katharsis.context.JsonApplicationContext;
import io.katharsis.repository.EntityRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RepositoryNotFoundException;
import io.katharsis.resource.ResourceInformation;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.annotations.JsonApiResource;
import net.jodah.typetools.TypeResolver;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Builder responsible for building an instance of ResourceRegistry.
 */
public class ResourceRegistryBuilder {

    private JsonApplicationContext context;
    private ResourceInformationBuilder resourceInformationBuilder;

    public ResourceRegistryBuilder(JsonApplicationContext context, ResourceInformationBuilder resourceInformationBuilder) {
        this.context = context;
        this.resourceInformationBuilder = resourceInformationBuilder;
    }

    /**
     * Scans all classes in provided package and finds all resources and repositories associated with found resource.
     *
     * @param packageName Package containing resources (models) and repositories.
     */
    public ResourceRegistry build(String packageName, String serviceUrl) {
        Reflections reflections = new Reflections(packageName);

        Set<Class<?>> jsonApiResources = reflections.getTypesAnnotatedWith(JsonApiResource.class);
        Set<Class<? extends EntityRepository>> entityRepositoryClasses = reflections.getSubTypesOf(EntityRepository.class);
        Set<Class<? extends RelationshipRepository>> relationshipRepositoryClasses = reflections
                .getSubTypesOf(RelationshipRepository.class);

        ResourceRegistry resourceRegistry = new ResourceRegistry(serviceUrl);
        for (Class resourceClass : jsonApiResources) {
            Class<? extends EntityRepository> foundEntityRepositoryClass = findEntityRepository(resourceClass, entityRepositoryClasses);
            Set<Class<? extends RelationshipRepository>> foundRelationshipRepositoriesClasses =
                    findRelationshipRepositories(resourceClass, relationshipRepositoryClasses);

            RegistryEntry registryEntry = createEntry(resourceClass, foundEntityRepositoryClass, foundRelationshipRepositoriesClasses);
            resourceRegistry.addEntry(resourceClass, registryEntry);
        }

        return resourceRegistry;
    }

    private Class<? extends EntityRepository> findEntityRepository(Class resourceClass,
                                                                   Set<Class<? extends EntityRepository>> entityRepositoryClasses) {
        for (Class<? extends EntityRepository> entityRepositoryClass : entityRepositoryClasses) {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(EntityRepository.class, entityRepositoryClass);
            if (typeArgs[0] == resourceClass) {
                return entityRepositoryClass;
            }
        }
        throw new RepositoryNotFoundException("Repository for resource not found: " + resourceClass.getCanonicalName());
    }

    private Set<Class<? extends RelationshipRepository>> findRelationshipRepositories(Class resourceClass,
                                                                                      Set<Class<? extends RelationshipRepository>> relationshipRepositoryClasses) {
        Set<Class<? extends RelationshipRepository>> foundRelationshipRepositories = new HashSet<>();
        for (Class<? extends RelationshipRepository> relationshipRepository : relationshipRepositoryClasses) {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, relationshipRepository);
            if (typeArgs[0] == resourceClass) {
                foundRelationshipRepositories.add(relationshipRepository);
            }
        }
        return foundRelationshipRepositories;
    }

    private RegistryEntry createEntry(Class resourceClass, Class<? extends EntityRepository> foundEntityRepositoryClass,
                                      Set<Class<? extends RelationshipRepository>> foundRelationshipRepositoriesClasses) {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(resourceClass);

        EntityRepository entityRepository = context.getInstance(foundEntityRepositoryClass);
        if (entityRepository == null) {
            throw new RepositoryNotFoundException("Instance of the repository not found: " +
                    foundEntityRepositoryClass.getCanonicalName());
        }
        List<RelationshipRepository> relationshipRepositories = new LinkedList<>();
        for (Class<? extends RelationshipRepository> relationshipRepositoryClass : foundRelationshipRepositoriesClasses) {
            RelationshipRepository relationshipRepository = context.getInstance(relationshipRepositoryClass);
            if (relationshipRepository == null) {
                throw new RepositoryNotFoundException("Instance of the repository not found: " +
                        foundEntityRepositoryClass.getCanonicalName());
            }
            relationshipRepositories.add(relationshipRepository);
        }
        return new RegistryEntry(resourceInformation, entityRepository, relationshipRepositories);
    }
}
