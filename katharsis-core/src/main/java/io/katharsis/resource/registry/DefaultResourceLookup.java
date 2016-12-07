package io.katharsis.resource.registry;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.reflections.Reflections;

import io.katharsis.queryspec.QuerySpecBulkRelationshipRepository;
import io.katharsis.queryspec.QuerySpecRelationshipRepository;
import io.katharsis.queryspec.QuerySpecRelationshipRepositoryBase;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.queryspec.QuerySpecResourceRepositoryBase;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.RelationshipRepositoryBase;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.resource.annotations.JsonApiResource;

/**
 * Scans all classes in provided package and finds all resources and repositories associated with found resource.
 */
public class DefaultResourceLookup implements ResourceLookup {

	private Reflections reflections;

	public DefaultResourceLookup(String packageName) {
		if (packageName != null) {
			String[] packageNamesArray = packageName.split(",");
			reflections = new Reflections((Object[]) packageNamesArray);
		}
		else {
			reflections = new Reflections(packageName);
		}
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
		Set<Class<? extends RelationshipRepository>> relationshipRepositories = reflections
				.getSubTypesOf(RelationshipRepository.class);
		Set<Class<? extends QuerySpecResourceRepository>> querySpecResourceRepositories = reflections
				.getSubTypesOf(QuerySpecResourceRepository.class);
		Set<Class<? extends QuerySpecRelationshipRepository>> querySpecRelationshipRepositories = reflections
				.getSubTypesOf(QuerySpecRelationshipRepository.class);

		Set<Class<?>> result = new HashSet<>();
		result.addAll(annotatedResourceRepositories);
		result.addAll(annotatedRelationshipRepositories);
		result.addAll(resourceRepositories);
		result.addAll(relationshipRepositories);
		result.addAll(querySpecResourceRepositories);
		result.addAll(querySpecRelationshipRepositories);
		result.addAll(reflections.getSubTypesOf(QuerySpecBulkRelationshipRepository.class));
		result.addAll(reflections.getSubTypesOf(QuerySpecResourceRepositoryBase.class));
		result.addAll(reflections.getSubTypesOf(QuerySpecRelationshipRepositoryBase.class));
		result.addAll(reflections.getSubTypesOf(RelationshipRepositoryBase.class));
		result.addAll(reflections.getSubTypesOf(ResourceRepositoryBase.class));
		
		// exclude interfaces an abstract base classes
		Iterator<Class<?>> iterator = result.iterator();
		while(iterator.hasNext()){
			Class<?> repoClass = iterator.next();
			if(repoClass.isInterface() || Modifier.isAbstract(repoClass.getModifiers())){
				iterator.remove();
			}
		}
		
		return result;
	}
}