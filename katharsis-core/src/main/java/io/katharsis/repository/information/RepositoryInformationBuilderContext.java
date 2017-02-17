package io.katharsis.repository.information;

import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.utils.parser.TypeParser;

public interface RepositoryInformationBuilderContext {

	ResourceInformationBuilder getResourceInformationBuilder();

	TypeParser getTypeParser();
}
