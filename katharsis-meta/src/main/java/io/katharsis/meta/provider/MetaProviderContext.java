package io.katharsis.meta.provider;

import java.util.Collection;

import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.utils.parser.TypeParser;

public interface MetaProviderContext {

	public void add(MetaElement element);

	public void addAll(Collection<? extends MetaElement> elements);

	public MetaLookup getLookup();

}
