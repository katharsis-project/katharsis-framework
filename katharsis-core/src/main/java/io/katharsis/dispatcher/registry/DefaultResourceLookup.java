package io.katharsis.dispatcher.registry;

import com.google.common.collect.Sets;
import io.katharsis.errorhandling.exception.KatharsisInitializationException;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.registry.ResourceLookup;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.katharsis.dispatcher.registry.AnnotationHelpers.getAnnotation;
import static io.katharsis.dispatcher.registry.AnnotationHelpers.getResourceType;

/**
 * Looks up and loads Json APi resource and repository classes.
 */
@Data
@Slf4j
public class DefaultResourceLookup implements ResourceLookup {

    @Override
    public ResourceRegistry scan(@NonNull String[] packages) {
        Map<String, Class<?>> resources = processResourceClasses(findResourceClasses(packages));
        Map<String, Class<?>> repositories = processRepositoryClasses(resources, findRepositoryClasses(packages));
        Map<String, Map<String, Class<?>>> relationships =
                processRelationshipClasses(resources, repositories, findRelationshipRepositoryClasses(packages));

        return new RegistryDataHolder(resources, repositories, relationships);
    }

    @Override
    public Set<Class<?>> getResourceClasses() {
        return Sets.newHashSet();
    }

    @Override
    public Set<Class<?>> getResourceRepositoryClasses() {
        return Sets.newHashSet();
    }

    protected Map<String, Class<?>> processResourceClasses(Set<Class<?>> classes) {
        Map<String, Class<?>> resources = new HashMap<>();
        for (Class resource : classes) {
            JsonApiResource res = getAnnotation(resource, JsonApiResource.class);
            if (resources.containsKey(res.type())) {
                log.error("Duplicate resource found for {}: {} and {}", res.type(),
                        resource.getCanonicalName(), resources.get(res.type()).getCanonicalName());
                throw new KatharsisInitializationException("Duplicate resource found for " + res.type());
            } else {
                log.info("Found JSON-API resource\t '{}': {}", res.type(), resource.getCanonicalName());
                resources.put(res.type(), resource);
            }
        }
        return resources;
    }

    protected Map<String, Class<?>> processRepositoryClasses(Map<String, Class<?>> resources, Set<Class<?>> repositoryClasses) {
        Map<String, Class<?>> repositories = new HashMap<>();
        for (Class repository : repositoryClasses) {
            JsonApiResourceRepository res = getAnnotation(repository, JsonApiResourceRepository.class);

            String resourceName = getResourceType(res.value());
            checkResourceIsRegistered(resources, resourceName);

            if (repositories.containsKey(resourceName)) {
                log.error("Duplicate resource found for {}: {} and {}", resourceName, res.value().getCanonicalName(),
                        repositories.get(resourceName).getCanonicalName());
                throw new KatharsisInitializationException("Duplicate resource found for " + resourceName);
            } else {
                log.info("Found JSON-API repository\t '{}': {}", resourceName, repository.getCanonicalName());
                repositories.put(resourceName, repository);
            }
        }

        if (!resources.keySet().containsAll(repositories.keySet())) {
            log.warn("There are resources without repositories.");
        }

        return repositories;
    }

    protected Map<String, Map<String, Class<?>>> processRelationshipClasses(@NonNull Map<String, Class<?>> resources,
                                                                            @NonNull Map<String, Class<?>> repositories,
                                                                            @NonNull Set<Class<?>> relationshipRepositoryClasses) {
        Map<String, Map<String, Class<?>>> relationships = new HashMap<>();

        for (Class relationshipRepo : relationshipRepositoryClasses) {
            JsonApiRelationshipRepository res = getAnnotation(relationshipRepo, JsonApiRelationshipRepository.class);

            String source = getResourceType(res.source());
            String target = getResourceType(res.target());

            checkResourceIsRegistered(resources, source);
            checkResourceIsRegistered(resources, target);

            checkRepositoryExists(repositories, source);
            checkRepositoryExists(repositories, target);

            Map<String, Class<?>> repos = getOrInit(relationships, source);
            repos.put(target, res.target());
            relationships.put(source, repos);
        }

        return relationships;
    }

    private Map<String, Class<?>> getOrInit(Map<String, Map<String, Class<?>>> relationships, String source) {
        Map<String, Class<?>> repos = relationships.get(source);
        if (repos == null) {
            repos = new HashMap<>();
        }
        return repos;
    }

    private String checkRepositoryExists(Map<String, Class<?>> repositories, @NonNull String resource) throws KatharsisInitializationException {
        if (!repositories.containsKey(resource)) {
            throw new KatharsisInitializationException("A relationship repository needs repositories for `source` " +
                    "and `target` resources. \nMissing repository for " + resource);
        }
        return resource;
    }

    private String checkResourceIsRegistered(Map<String, Class<?>> resources, String resourceName) {
        if (!resources.containsKey(resourceName)) {
            throw new KatharsisInitializationException("Resource is not known in this registry: " + resourceName);
        }
        return resourceName;
    }

    protected Set<Class<?>> findResourceClasses(String[] packages) {
        return new Reflections(packages).getTypesAnnotatedWith(JsonApiResource.class);
    }

    protected Set<Class<?>> findRepositoryClasses(String[] packages) {
        return new Reflections(packages).getTypesAnnotatedWith(JsonApiResourceRepository.class);
    }

    protected Set<Class<?>> findRelationshipRepositoryClasses(String[] packages) {
        return new Reflections(packages).getTypesAnnotatedWith(JsonApiRelationshipRepository.class);
    }

}
