package io.katharsis.servlet.resource.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.servlet.resource.model.Node;


public class NodeRepository extends AbstractRepo<Node, Long> {
	private static final Map<Long, Node> NODE_REPO = new ConcurrentHashMap<>();

	@Override
	protected Map<Long, Node> getRepo() {
		return NODE_REPO;
	}
}
