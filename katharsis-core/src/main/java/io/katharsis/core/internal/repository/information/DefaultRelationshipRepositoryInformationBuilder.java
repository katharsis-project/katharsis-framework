package io.katharsis.core.internal.repository.information;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.katharsis.repository.RelationshipRepositoryV2;
import io.katharsis.repository.information.RepositoryInformation;
import io.katharsis.repository.information.RepositoryInformationBuilder;
import io.katharsis.repository.information.RepositoryInformationBuilderContext;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.utils.Optional;
import net.jodah.typetools.TypeResolver;

public class DefaultRelationshipRepositoryInformationBuilder implements RepositoryInformationBuilder {

	@Override
	public boolean accept(Object repository) {
		Class<? extends Object> repositoryClass = repository.getClass();
		return accept(repositoryClass);
	}

	@Override
	public boolean accept(Class<?> repositoryClass) {
		return RelationshipRepository.class.isAssignableFrom(repositoryClass) || RelationshipRepositoryV2.class.isAssignableFrom(repositoryClass)
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

	private RepositoryInformation buildInformation(Object repository, Class<? extends Object> repositoryClass, RepositoryInformationBuilderContext context) {
		Class<?> sourceResourceClass = getSourceResourceClass(repository, repositoryClass);
		Class<?> targetResourceClass = getTargetResourceClass(repository, repositoryClass);

		PreconditionUtil.assertNotNull("no sourceResourceClass", sourceResourceClass);
		PreconditionUtil.assertNotNull("no targetResourceClass", targetResourceClass);

		ResourceInformationBuilder resourceInformationBuilder = context.getResourceInformationBuilder();
		PreconditionUtil.assertTrue("cannot get ResourceInformation for " + sourceResourceClass, resourceInformationBuilder.accept(sourceResourceClass));
		ResourceInformation sourceResourceInformation = resourceInformationBuilder.build(sourceResourceClass);

		ResourceInformation targetResourceInformation;
		if (resourceInformationBuilder.accept(targetResourceClass)) {
			targetResourceInformation = resourceInformationBuilder.build(targetResourceClass);
		} else {
			// support for polymorphism like relations to java.lang.Object
			targetResourceInformation = new ResourceInformation(context.getTypeParser(), targetResourceClass, null, null, null);
		}

		return new RelationshipRepositoryInformationImpl(repositoryClass, sourceResourceInformation, targetResourceInformation);
	}

	protected Class<?> getSourceResourceClass(Object repository, Class<?> repositoryClass) {
		Optional<JsonApiRelationshipRepository> annotation = ClassUtils.getAnnotation(repositoryClass, JsonApiRelationshipRepository.class);

		if (annotation.isPresent()) {
			return annotation.get().source();
		} else if (RelationshipRepository.class.isAssignableFrom(repositoryClass)) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, repositoryClass);
			return typeArgs[0];
		} else if (repository != null) {
			RelationshipRepositoryV2<?, ?, ?, ?> querySpecRepo = (RelationshipRepositoryV2<?, ?, ?, ?>) repository;
			return querySpecRepo.getSourceResourceClass();
		} else {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepositoryV2.class, repositoryClass);
			return typeArgs[0];
		}
	}

	protected Class<?> getTargetResourceClass(Object repository, Class<?> repositoryClass) {
		Optional<JsonApiRelationshipRepository> annotation = ClassUtils.getAnnotation(repositoryClass, JsonApiRelationshipRepository.class);

		if (annotation.isPresent()) {
			return annotation.get().target();
		} else if (RelationshipRepository.class.isAssignableFrom(repositoryClass)) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, repositoryClass);
			return typeArgs[2];
		} else if (repository != null) {
			RelationshipRepositoryV2<?, ?, ?, ?> querySpecRepo = (RelationshipRepositoryV2<?, ?, ?, ?>) repository;
			return querySpecRepo.getTargetResourceClass();
		} else {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepositoryV2.class, repositoryClass);
			return typeArgs[2];
		}
	}
}
