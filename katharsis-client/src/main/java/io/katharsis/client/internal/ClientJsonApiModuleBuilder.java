package io.katharsis.client.internal;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.katharsis.client.internal.core.BaseResponseSerializer;
import io.katharsis.client.internal.core.ContainerSerializer;
import io.katharsis.client.internal.core.ErrorResponseSerializer;
import io.katharsis.client.internal.core.LinkageContainerSerializer;
import io.katharsis.client.internal.core.RelationshipContainerSerializer;
import io.katharsis.resource.registry.ResourceRegistry;

public class ClientJsonApiModuleBuilder {

	public static final String JSON_API_MODULE_NAME = "JsonApiClientModule";

	/**
	 * Creates Katharsis Jackson module with all required serializers
	 *
	 * @param resourceRegistry initialized registry with all of the required resources
	 * @param isClient         is katharsis client
	 * @return {@link com.fasterxml.jackson.databind.Module} with custom serializers
	 */
	public SimpleModule build(ResourceRegistry resourceRegistry, boolean isClient) {
		SimpleModule simpleModule = new SimpleModule(JSON_API_MODULE_NAME, new Version(1, 0, 0, null, null, null));

		simpleModule.addSerializer(new ContainerSerializer(resourceRegistry, isClient))
				.addSerializer(new ClientDataLinksContainerSerializer())
				.addSerializer(new RelationshipContainerSerializer(resourceRegistry, isClient))
				.addSerializer(new LinkageContainerSerializer()).addSerializer(new BaseResponseSerializer(resourceRegistry))
				.addSerializer(new ErrorResponseSerializer());

		return simpleModule;
	}
}