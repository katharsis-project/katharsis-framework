package io.katharsis.queryspec;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

public abstract class QuerySpecBulkRelationshipRepositoryBase<T, I extends Serializable, D, J extends Serializable>
		implements QuerySpecBulkRelationshipRepository<T, I, D, J> {

	@Override
	public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		Map<I, D> map = findOneTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return null;
		}
		else if (map.containsKey(sourceId) && map.size() == 1) {
			return map.get(sourceId);
		}
		else {
			throw new IllegalStateException("expected sourceId=" + sourceId + "in result " + map);
		}
	}

	@Override
	public Iterable<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		Map<I, Iterable<D>> map = findManyTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return null;
		}
		else if (map.containsKey(sourceId) && map.size() == 1) {
			return map.get(sourceId);
		}
		else {
			throw new IllegalStateException("expected sourceId=" + sourceId + "in result " + map);
		}
	}

}
