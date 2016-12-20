package io.katharsis.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResourceId implements Comparable<ResourceId> {

	private String id;

	private String type;

	public ResourceId() {
	}

	public ResourceId(String id, String type) {
		this.id = id;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public ResourceId clone() {
		return new ResourceId(id, type);
	}

	public static Object fromData(Object data) {
		if (data == null) {
			return null;
		}
		if (data instanceof Iterable) {
			List<ResourceId> result = new ArrayList<>();
			for (ResourceId id : (Iterable<ResourceId>) data) {
				result.add(id.clone());
			}
			return result;
		} else {
			ResourceId id = (ResourceId) data;
			return id.clone();
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ResourceId))
			return false;
		ResourceId other = (ResourceId) obj;
		return Objects.equals(id, other.id) && Objects.equals(type, other.type);
	}

	@Override
	public int compareTo(ResourceId o) {
		int d = type.compareTo(o.type);
		if (d != 0) {
			return d;
		}
		return id.compareTo(o.id);
	}
}