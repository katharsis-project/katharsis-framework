package io.katharsis.jpa.util;

import java.util.Comparator;

import io.katharsis.resource.information.ResourceField;

public class ResourceFieldComparator implements Comparator<ResourceField> {

	public static final ResourceFieldComparator INSTANCE = new ResourceFieldComparator();

	@Override
	public int compare(ResourceField o1, ResourceField o2) {
		return o1.getJsonName().compareTo(o2.getJsonName());
	}
}