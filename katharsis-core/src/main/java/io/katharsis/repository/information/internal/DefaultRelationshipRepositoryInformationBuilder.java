package io.katharsis.repository.information.internal;

import io.katharsis.queryspec.QuerySpecRelationshipRepository;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.information.RepositoryInformation;
import io.katharsis.repository.information.RepositoryInformationBuilder;
import io.katharsis.repository.information.RepositoryInformationBuilderContext;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.PreconditionUtil;
import io.katharsis.utils.java.Optional;
import net.jodah.typetools.TypeResolver;

public class DefaultRelationshipRepositoryInformationBuilder implements RepositoryInformationBuilder {

	@Override
	public boolean accept(Object repository) {
		Class<? extends Object> repositoryClass = repository.getClass();
		return accept(repositoryClass);
	}

	@Override
	public boolean accept(Class<?> repositoryClass) {
		return RelationshipRepository.class.isAssignableFrom(repositoryClass)
				|| QuerySpecRelationshipRepository.class.isAssignableFrom(repositoryClass)
				|| ClassUtils.getAnnotation(repositoryClass, JsonApiRelationshipRepository.class).isPresent();
	}

	@Override
	public RepositoryInformation build(Object repository, RepositoryInformationBuilderContext context) {
		return buildInformation(repository, repository.getClass(), context);
	}

	@Override
	public RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationBuilderContext context) {
		return buildInformation(null, repositoryClass, context);
	}

	private RepositoryInformation buildInformation(Object repository, Class<? extends Object> repositoryClass,
			RepositoryInformationBuilderContext context) {
		Class<?> sourceResourceClass = getSourceResourceClass(repository, repository.getClass());
		Class<?> targetResourceClass = getTargetResourceClass(repository, repository.getClass());

		PreconditionUtil.assertNotNull("no sourceResourceClass", sourceResourceClass);
		PreconditionUtil.assertNotNull("no targetResourceClass", targetResourceClass);

		ResourceInformationBuilder resourceInformationBuilder = context.getResourceInformationBuilder();
		PreconditionUtil.assertTrue("cannot get ResourceInformation for " + sourceResourceClass,
				resourceInformationBuilder.accept(sourceResourceClass));
		ResourceInformation sourceResourceInformation = resourceInformationBuilder.build(sourceResourceClass);

		ResourceInformation targetResourceInformation;
		if (resourceInformationBuilder.accept(targetResourceClass)) {
			targetResourceInformation = resourceInformationBuilder.build(targetResourceClass);
		}
		else {
			// support for polymorphism like relations to java.lang.Object
			targetResourceInformation = new ResourceInformation(targetResourceClass, null, null, null, null);
		}

		return new RelationshipRepositoryInformationImpl(repositoryClass, sourceResourceInformation, targetResourceInformation);
	}

	protected Class<?> getSourceResourceClass(Object repository, Class<?> repositoryClass) {
		Optional<JsonApiRelationshipRepository> annotation = ClassUtils.getAnnotation(repositoryClass,
				JsonApiRelationshipRepository.class);

		if (annotation.isPresent()) {
			return annotation.get().source();
		}
		else if (RelationshipRepository.class.isAssignableFrom(repositoryClass)) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, repositoryClass);
			return typeArgs[0];
		}
		else if (repository != null) {
			QuerySpecRelationshipRepository<?, ?, ?, ?> querySpecRepo = (QuerySpecRelationshipRepository<?, ?, ?, ?>) repository;
			return querySpecRepo.getSourceResourceClass();
		}
		else {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(QuerySpecRelationshipRepository.class, repositoryClass);
			return typeArgs[0];
		}
	}

	protected Class<?> getTargetResourceClass(Object repository, Class<?> repositoryClass) {
		Optional<JsonApiRelationshipRepository> annotation = ClassUtils.getAnnotation(repositoryClass,
				JsonApiRelationshipRepository.class);

		if (annotation.isPresent()) {
			return annotation.get().target();
		}
		else if (RelationshipRepository.class.isAssignableFrom(repositoryClass)) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, repositoryClass);
			return typeArgs[2];
		}
		else if (repository != null) {
			QuerySpecRelationshipRepository<?, ?, ?, ?> querySpecRepo = (QuerySpecRelationshipRepository<?, ?, ?, ?>) repository;
			return querySpecRepo.getTargetResourceClass();
		}
		else {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(QuerySpecRelationshipRepository.class, repositoryClass);
			return typeArgs[2];
		}
	}
}
