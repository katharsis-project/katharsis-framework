package io.katharsis.resource.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.katharsis.module.Module;

/**
 * Combines all {@link ResourceLookup} instances provided by the registered
 * {@link Module}.
 */
public class MultiResourceLookup implements ResourceLookup {

	private Collection<ResourceLookup> lookups;

	public MultiResourceLookup(List<ResourceLookup> lookups) {
		this.lookups = lookups;
	}

	@Override
	public Set<Class<?>> getResourceClasses() {
		Set<Class<?>> set = new HashSet<>();
		for (ResourceLookup lookup : lookups) {
			set.addAll(lookup.getResourceClasses());
		}
		return set;
	}

	@Override
	public Set<Class<?>> getResourceRepositoryClasses() {
		Set<Class<?>> set = new HashSet<>();
		for (ResourceLookup lookup : lookups) {
			set.addAll(lookup.getResourceRepositoryClasses());
		}
		return set;
	}

	public static ResourceLookup newInstance(ResourceLookup... lookups) {
		List<ResourceLookup> list = new ArrayList<>();
		for (ResourceLookup lookup : lookups) {
			if (lookup != null) {
				list.add(lookup);
			}
		}
		return new MultiResourceLookup(list);
	}
}