package io.katharsis.resource.mock.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.resource.mock.models.Group;
import io.katharsis.resource.mock.models.Memorandum;
import io.katharsis.resource.mock.repository.util.Relation;

public class GroupToMemoRepository extends AbstractRelationShipRepository<Group> implements
		RelationshipRepository<Group, Long, Memorandum, Long> {

	private final static ConcurrentMap<Relation<Group>, Integer> STATIC_REPOSITORY = new ConcurrentHashMap<>();

	public static void clear() {
		STATIC_REPOSITORY.clear();
	}

	@Override
	ConcurrentMap<Relation<Group>, Integer> getRepo() {
		return STATIC_REPOSITORY;
	}

	@Override
	public Memorandum findOneTarget(Long sourceId, String fieldName, QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<Memorandum> findManyTargets(Long sourceId, String fieldName, QueryParams queryParams) {
		List<Memorandum> memos = new LinkedList<>();
		for (Relation<Group> relation : getRepo().keySet()) {
			if (relation.getSource().getId().equals(sourceId) && relation.getFieldName().equals(fieldName)) {
				Memorandum memo = new Memorandum();
				memo.setId((Long) relation.getTargetId());
				memos.add(memo);
			}
		}
		return memos;
	}
}
