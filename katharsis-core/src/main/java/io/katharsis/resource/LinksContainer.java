package io.katharsis.resource;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface LinksContainer {

	public ObjectNode getLinks();

	public void setLinks(ObjectNode meta);
}
