package io.katharsis.repository.information.internal;

import java.util.HashMap;
import java.util.Map;

import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.repository.information.RepositoryAction;
import io.katharsis.repository.information.RepositoryInformation;
import io.katharsis.repository.information.RepositoryInformationBuilder;
import io.katharsis.repository.information.RepositoryInformationBuilderContext;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.PreconditionUtil;
import io.katharsis.utils.java.Optional;
import net.jodah.typetools.TypeResolver;

public class DefaultResourceRepositoryInformationBuilder implements RepositoryInformationBuilder {

	@Override
	public boolean accept(Object repository) {
		Class<? extends Object> repositoryClass = repository.getClass();
		return accept(repositoryClass);
	}

	@Override
	public boolean accept(Class<?> repositoryClass) {
		boolean legacyRepo = ResourceRepository.class.isAssignableFrom(repositoryClass);
		boolean interfaceRepo = QuerySpecResourceRepository.class.isAssignableFrom(repositoryClass);
		boolean anontationRepo = ClassUtils.getAnnotation(repositoryClass, JsonApiResourceRepository.class).isPresent();
		return legacyRepo || interfaceRepo || anontationRepo;
	}

	@Override
	public RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationBuilderContext context) {
		return build(null, repositoryClass, context);
	}

	@Override
	public RepositoryInformation build(Object repository, RepositoryInformationBuilderContext context) {
		return build(repository, repository.getClass(), context);
	}

	private RepositoryInformation build(Object repository, Class<? extends Object> repositoryClass,
			RepositoryInformationBuilderContext context) {
		Class<?> resourceClass = getResourceClass(repository, repositoryClass);

		ResourceInformationBuilder resourceInformationBuilder = context.getResourceInformationBuilder();
		PreconditionUtil.assertTrue("cannot get ResourceInformation for " + resourceClass,
				resourceInformationBuilder.accept(resourceClass));
		ResourceInformation resourceInformation = resourceInformationBuilder.build(resourceClass);
		String path = getPath(resourceInformation, repository);

		return new ResourceRepositoryInformationImpl(repositoryClass, path, resourceInformation, buildActions(repositoryClass));
	}

	protected Map<String, RepositoryAction> buildActions(Class<? extends Object> repositoryClass) {
		return new HashMap<>();
	}

	protected String getPath(ResourceInformation resourceInformation, Object repository) { // NOSONAR contract ok
		return resourceInformation.getResourceType();
	}

	protected Class<?> getResourceClass(Object repository, Class<?> repositoryClass) {
		Optional<JsonApiResourceRepository> annotation = ClassUtils.getAnnotation(repositoryClass,
				JsonApiResourceRepository.class);

		if (annotation.isPresent()) {
			return annotation.get().value();
		}
		else if (repository instanceof ResourceRepository) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepository.class, repository.getClass());
			return typeArgs[0];
		}
		else if (repository != null) {
			QuerySpecResourceRepository<?, ?> querySpecRepo = (QuerySpecResourceRepository<?, ?>) repository;
			return querySpecRepo.getResourceClass();
		}
		else {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(QuerySpecResourceRepository.class, repositoryClass);
			return typeArgs[0];
		}
	}
}
