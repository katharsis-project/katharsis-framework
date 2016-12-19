package io.katharsis.resource;

import java.util.ArrayList;
import java.util.List;

public class ResourceId {

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
	public ResourceId clone(){
		return new ResourceId(id, type);
	}
	
	public static Object fromData(Object data) {
		if (data == null) {
			return null;
		}
		if (data instanceof Iterable) {
			List<ResourceId> result = new ArrayList<>();
			for(ResourceId id : (Iterable<ResourceId>) data){
				result.add(id.clone());
			}
			return result;
		} else {
			ResourceId id = (ResourceId) data;
			return id.clone();
		}
	}
}