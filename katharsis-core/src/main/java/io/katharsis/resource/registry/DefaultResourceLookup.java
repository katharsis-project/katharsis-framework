package io.katharsis.resource.registry;

import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;

import io.katharsis.queryspec.QuerySpecRelationshipRepository;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.resource.annotations.JsonApiResource;

/**
 * Scans all classes in provided package and finds all resources and repositories associated with found resource.
 */
public class DefaultResourceLookup implements ResourceLookup {

	private String packageName;
	private Reflections reflections;

	public DefaultResourceLookup(String packageName) {
		this.packageName = packageName;
        if (packageName != null) {
            String[] packageNames = packageName.split(",");
            reflections = new Reflections(packageNames);
        } else {
            reflections = new Reflections(packageName);
        }
	}

	@Override
	public Set<Class<?>> getResourceClasses() {
        return reflections.getTypesAnnotatedWith(JsonApiResource.class);
	}
	
	@Override
	public Set<Class<?>> getResourceRepositoryClasses() {
		Set<Class<?>> annotatedResourceRepositories = reflections.getTypesAnnotatedWith(JsonApiResourceRepository.class);
		Set<Class<?>> annotatedRelationshipRepositories = reflections.getTypesAnnotatedWith(JsonApiRelationshipRepository.class);
		Set<Class<? extends ResourceRepository>> resourceRepositories = reflections.getSubTypesOf(ResourceRepository.class);
		Set<Class<? extends RelationshipRepository>> relationshipRepositories = reflections.getSubTypesOf(RelationshipRepository.class);
		Set<Class<? extends QuerySpecResourceRepository>> querySpecResourceRepositories = reflections.getSubTypesOf(QuerySpecResourceRepository.class);
		Set<Class<? extends QuerySpecRelationshipRepository>> querySpecRelationshipRepositories = reflections.getSubTypesOf(QuerySpecRelationshipRepository.class);

		Set<Class<?>> result = new HashSet<>();
		result.addAll(annotatedResourceRepositories);
		result.addAll(annotatedRelationshipRepositories);
		result.addAll(resourceRepositories);
		result.addAll(relationshipRepositories);
		result.addAll(querySpecResourceRepositories);
		result.addAll(querySpecRelationshipRepositories);
		return result;
	}
}