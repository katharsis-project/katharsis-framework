package io.katharsis.meta.provider;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import io.katharsis.meta.model.MetaElement;

public class MetaProviderBase implements MetaProvider {

	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		return false;
	}

	@Override
	public MetaElement createElement(Type type, MetaProviderContext context) {
		// does not accept anything, so does not need to create anything
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<MetaProvider> getDependencies() {
		return Collections.emptySet();
	}

	@Override
	public void discoverElements(MetaProviderContext context) {
		// nothing to do
	}

	@Override
	public void onInitializing(MetaProviderContext context, MetaElement element) {
		// nothing to do
	}

	@Override
	public void onInitialized(MetaProviderContext context, MetaElement element) {
		// nothing to do
	}

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		return Collections.emptySet();
	}

	@Override
	public Map<? extends String, ? extends String> getIdMappings() {
		return Collections.emptyMap();
	}
}
