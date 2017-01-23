package io.katharsis.client.dynamic;

import java.util.ArrayList;
import java.util.List;

import io.katharsis.core.internal.repository.information.ResourceRepositoryInformationImpl;
import io.katharsis.core.internal.resource.ResourceFieldImpl;
import io.katharsis.repository.information.RepositoryInformation;
import io.katharsis.repository.information.RepositoryInformationBuilder;
import io.katharsis.repository.information.RepositoryInformationBuilderContext;
import io.katharsis.resource.Resource;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceFieldType;
import io.katharsis.resource.information.ResourceInformation;

public class DynamicRepositoryInformationBuilder implements RepositoryInformationBuilder {

	@Override
	public boolean accept(Class<?> repositoryClass) {
		return false;
	}

	@Override
	public boolean accept(Object repository) {
		return repository instanceof DynamicRepository;
	}

	@Override
	public RepositoryInformation build(Object repositoryObj, RepositoryInformationBuilderContext context) {
		DynamicRepository repository = (DynamicRepository) repositoryObj;

		List<ResourceField> fields = new ArrayList<>();
		fields.add(new ResourceFieldImpl("id", "id", ResourceFieldType.ID, String.class, String.class, null));
		fields.add(new ResourceFieldImpl("value", "value", ResourceFieldType.ATTRIBUTE, String.class, String.class, null));
		ResourceInformation resourceInformation = new ResourceInformation(Resource.class, repository.getResourceType(), fields);

		String path = repository.getResourceType();
		
		return new ResourceRepositoryInformationImpl(repositoryObj.getClass(), path, resourceInformation);
	}

	@Override
	public RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationBuilderContext context) {
		throw new UnsupportedOperationException();
	}
}
