package io.katharsis.repository.information.internal;

import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
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
		boolean legacyRepo = ResourceRepository.class.isAssignableFrom(repositoryClass);
		boolean interfaceRepo = QuerySpecResourceRepository.class.isAssignableFrom(repositoryClass);
		boolean anontationRepo = ClassUtils.getAnnotation(repositoryClass, JsonApiResourceRepository.class).isPresent();
		return legacyRepo || interfaceRepo || anontationRepo;
	}

	@Override
	public RepositoryInformation build(Object repository, RepositoryInformationBuilderContext context) {
		Class<?> resourceClass = getResourceClass(repository);

		ResourceInformationBuilder resourceInformationBuilder = context.getResourceInformationBuilder();
		PreconditionUtil.assertTrue("cannot get ResourceInformation for " + resourceClass,
				resourceInformationBuilder.accept(resourceClass));
		ResourceInformation resourceInformation = resourceInformationBuilder.build(resourceClass);
		String path = getPath(resourceInformation, repository);

		return new ResourceRepositoryInformationImpl(repository, path, resourceInformation);
	}

	protected String getPath(ResourceInformation resourceInformation, Object repository) { // NOSONAR contract ok
		return resourceInformation.getResourceType();
	}

	protected Class<?> getResourceClass(Object repository) {
		Optional<JsonApiResourceRepository> annotation = ClassUtils.getAnnotation(repository.getClass(),
				JsonApiResourceRepository.class);

		if (annotation.isPresent()) {
			return annotation.get().value();
		}
		else if (repository instanceof ResourceRepository) {
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepository.class, repository.getClass());
			return typeArgs[0];
		}
		else {
			QuerySpecResourceRepository<?, ?> querySpecRepo = (QuerySpecResourceRepository<?, ?>) repository;
			return querySpecRepo.getResourceClass();
		}
	}

}
