package io.katharsis.resource.registry;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.reflections.Reflections;

import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.legacy.repository.ResourceRepository;
import io.katharsis.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.legacy.repository.annotations.JsonApiResourceRepository;
import io.katharsis.repository.BulkRelationshipRepositoryV2;
import io.katharsis.repository.RelationshipRepositoryBase;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.ResourceRepositoryBase;
import io.katharsis.repository.ResourceRepositoryV2;
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
		Set<Class<? extends ResourceRepositoryV2>> querySpecResourceRepositories = reflections
				.getSubTypesOf(ResourceRepositoryV2.class);
		Set<Class<? extends RelationshipRepositoryV2>> querySpecRelationshipRepositories = reflections
				.getSubTypesOf(RelationshipRepositoryV2.class);

		Set<Class<?>> result = new HashSet<>();
		result.addAll(annotatedResourceRepositories);
		result.addAll(annotatedRelationshipRepositories);
		result.addAll(resourceRepositories);
		result.addAll(relationshipRepositories);
		result.addAll(querySpecResourceRepositories);
		result.addAll(querySpecRelationshipRepositories);
		result.addAll(reflections.getSubTypesOf(BulkRelationshipRepositoryV2.class));
		result.addAll(reflections.getSubTypesOf(ResourceRepositoryBase.class));
		result.addAll(reflections.getSubTypesOf(RelationshipRepositoryBase.class));
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