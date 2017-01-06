package io.katharsis.servlet.resource.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.servlet.resource.model.NodeComment;

/**
 * Created by nickmitchell on 1/6/17.
 */
public class NodeCommentRepository implements ResourceRepository<NodeComment, Long> {

	private static Map<Long, NodeComment> NODE_COMMENT_REPO = new ConcurrentHashMap<>();

	@Override
	public NodeComment findOne(Long id, QueryParams queryParams) {
		return NODE_COMMENT_REPO.get(id);
	}

	@Override
	public Iterable<NodeComment> findAll(QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<NodeComment> findAll(Iterable<Long> longs, QueryParams queryParams) {
		return NODE_COMMENT_REPO.values();
	}

	@Override
	public <S extends NodeComment> S save(S entity) {
		return (S) NODE_COMMENT_REPO.put(entity.getId(), entity);
	}

	@Override
	public void delete(Long id) {
		NODE_COMMENT_REPO.remove(id);
	}
}
