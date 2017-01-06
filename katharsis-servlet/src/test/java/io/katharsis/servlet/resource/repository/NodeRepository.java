package io.katharsis.servlet.resource.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.servlet.resource.model.Node;

/**
 * Created by nickmitchell on 1/5/17.
 */
public class NodeRepository implements ResourceRepository<Node, Long> {
	private static Map<Long, Node> NODE_REPO = new ConcurrentHashMap<>();

	@Override
	public Node findOne(Long id, QueryParams queryParams) {
		return NODE_REPO.get(id);
	}

	@Override
	public Iterable<Node> findAll(QueryParams queryParams) {
		return NODE_REPO.values();
	}

	@Override
	public Iterable<Node> findAll(Iterable<Long> ids, QueryParams queryParams) {
		return null;
	}

	@Override
	public <S extends Node> S save(S entity) {
		NODE_REPO.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public void delete(Long id) {
		NODE_REPO.remove(id);
	}

	public void clearRepo() {
		NODE_REPO.clear();
	}
}
