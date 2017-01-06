package io.katharsis.servlet.resource.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.servlet.resource.model.NodeComment;

public class NodeCommentRepository extends AbstractRepo<NodeComment, Long> {
	private static final Map<Long, NodeComment> NODE_COMMENT_REPO = new ConcurrentHashMap<>();
	@Override
	protected Map<Long, NodeComment> getRepo() {
		return NODE_COMMENT_REPO;
	}
}
