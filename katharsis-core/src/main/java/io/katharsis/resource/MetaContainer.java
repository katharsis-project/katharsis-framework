package io.katharsis.resource;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface MetaContainer {

	public ObjectNode getMeta();

	public void setMeta(ObjectNode meta);
}
