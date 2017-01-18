package io.katharsis.meta.provider;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import io.katharsis.meta.model.MetaElement;

public interface MetaProvider {

	public Collection<MetaProvider> getDependencies();

	public Set<Class<? extends MetaElement>> getMetaTypes();

	public boolean accept(Type type, Class<? extends MetaElement> requestedMetaClass);

	public MetaElement createElement(Type type, MetaProviderContext context);

	public void discoverElements(MetaProviderContext context);

	public void onInitializing(MetaProviderContext context, MetaElement element);

	public void onInitialized(MetaProviderContext context, MetaElement element);

	public Map<? extends String, ? extends String> getIdMappings();

}
