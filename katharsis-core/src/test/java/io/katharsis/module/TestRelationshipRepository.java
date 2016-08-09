package io.katharsis.module;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RelationshipRepository;

class TestRelationshipRepository implements RelationshipRepository<TestResource, Integer, TestResource, Integer> {

	@Override
	public void setRelation(TestResource source, Integer targetId, String fieldName) {
	}

	@Override
	public void setRelations(TestResource source, Iterable<Integer> targetIds, String fieldName) {
	}

	@Override
	public void addRelations(TestResource source, Iterable<Integer> targetIds, String fieldName) {
	}

	@Override
	public void removeRelations(TestResource source, Iterable<Integer> targetIds, String fieldName) {
	}

	@Override
	public TestResource findOneTarget(Integer sourceId, String fieldName, QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<TestResource> findManyTargets(Integer sourceId, String fieldName, QueryParams queryParams) {
		return null;
	}
}