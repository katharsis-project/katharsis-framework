package io.katharsis.resource.registry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import io.katharsis.queryspec.QuerySpecBulkRelationshipRepository;
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

	private List<String> packageNames;
	private Reflections reflections;

	public DefaultResourceLookup(String packageName) {
        if (packageName != null) {
            String[] packageNamesArray = packageName.split(",");
            reflections = new Reflections((Object[])packageNamesArray);
           packageNames = Arrays.asList(packageNamesArray);
        } else {
            reflections = new Reflections(packageName);
        }
	}

	public DefaultResourceLookup(List<String> packageNames) {
		this.packageNames = packageNames;
        if (packageNames != null) {
            reflections = new Reflections((Object[])packageNames.toArray(new String[packageNames.size()]));
        } else {
            reflections = new Reflections((String)null);
        }
	}

	@Override
	public String toString(){
		return getClass().getName() + "[packageNames=" + packageNames + "]";
	}
	
	
	@Override
	public Set<Class<?>> getResourceClasses() {
        return reflections.getTypesAnnotatedWith(JsonApiResource.class);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Set<Class<?>> getResourceRepositoryClasses() {
		Set<Class<?>> annotatedResourceRepositories = reflections.getTypesAnnotatedWith(JsonApiResourceRepository.class);
		Set<Class<?>> annotatedRelationshipRepositories = reflections.getTypesAnnotatedWith(JsonApiRelationshipRepository.class);
		Set<Class<? extends ResourceRepository>> resourceRepositories = reflections.getSubTypesOf(ResourceRepository.class);
		Set<Class<? extends RelationshipRepository>> relationshipRepositories = reflections.getSubTypesOf(RelationshipRepository.class);
		Set<Class<? extends QuerySpecResourceRepository>> querySpecResourceRepositories = reflections.getSubTypesOf(QuerySpecResourceRepository.class);
		Set<Class<? extends QuerySpecRelationshipRepository>> querySpecRelationshipRepositories = reflections.getSubTypesOf(QuerySpecRelationshipRepository.class);
		Set<Class<? extends QuerySpecBulkRelationshipRepository>> querySpecBulkRelationshipRepositories = reflections.getSubTypesOf(QuerySpecBulkRelationshipRepository.class);

		Set<Class<?>> result = new HashSet<>();
		result.addAll(annotatedResourceRepositories);
		result.addAll(annotatedRelationshipRepositories);
		result.addAll(resourceRepositories);
		result.addAll(relationshipRepositories);
		result.addAll(querySpecResourceRepositories);
		result.addAll(querySpecRelationshipRepositories);
		result.addAll(querySpecBulkRelationshipRepositories);
		return result;
	}
}