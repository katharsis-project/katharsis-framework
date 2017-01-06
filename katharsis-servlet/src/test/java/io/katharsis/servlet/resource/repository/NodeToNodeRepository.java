package io.katharsis.servlet.resource.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RelationshipRepository;
import io.katharsis.servlet.resource.model.Node;

/**
 * Created by nickmitchell on 1/5/17.
 */
public class NodeToNodeRepository implements RelationshipRepository<Node, Long, Node, Long> {
	@Override
	public void setRelation(Node source, Long targetId, String fieldName) {

	}

	@Override
	public void setRelations(Node source, Iterable<Long> targetIds, String fieldName) {

	}

	@Override
	public void addRelations(Node source, Iterable<Long> targetIds, String fieldName) {

	}

	@Override
	public void removeRelations(Node source, Iterable<Long> targetIds, String fieldName) {

	}

	@Override
	public Node findOneTarget(Long sourceId, String fieldName, QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<Node> findManyTargets(Long sourceId, String fieldName, QueryParams queryParams) {
		return null;
	}
}
