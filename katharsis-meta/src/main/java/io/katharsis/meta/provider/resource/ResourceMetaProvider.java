package io.katharsis.meta.provider.resource;

import java.util.Arrays;
import java.util.Collection;

import io.katharsis.meta.internal.JsonObjectMetaProvider;
import io.katharsis.meta.internal.ResourceMetaProviderImpl;
import io.katharsis.meta.provider.MetaProvider;
import io.katharsis.meta.provider.MetaProviderBase;

public class ResourceMetaProvider extends MetaProviderBase {

	@Override
	public Collection<MetaProvider> getDependencies() {
		return Arrays.asList((MetaProvider) new ResourceMetaProviderImpl(), new JsonObjectMetaProvider());
	}
}
