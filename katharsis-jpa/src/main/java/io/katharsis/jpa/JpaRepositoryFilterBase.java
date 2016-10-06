package io.katharsis.jpa;

import java.util.List;

import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.Tuple;
import io.katharsis.queryspec.QuerySpec;

/**
 * Empty default implementation for {@link JpaRepositoryFilter}.
 */
public class JpaRepositoryFilterBase implements JpaRepositoryFilter {

	@Override
	public boolean accept(Class<?> resourceType) {
		return true;
	}

	@Override
	public QuerySpec filterQuerySpec(Object repository, QuerySpec querySpec) {
		return querySpec;
	}

	@Override
	public <T> JpaQuery<T> filterQuery(Object repository, QuerySpec querySpec, JpaQuery<T> query) {
		return query;
	}

	@Override
	public <T> JpaQueryExecutor<T> filterExecutor(Object repository, QuerySpec querySpec, JpaQueryExecutor<T> executor) {
		return executor;
	}

	@Override
	public List<Tuple> filterTuples(Object repository, QuerySpec querySpec, List<Tuple> tuples) {
		return tuples;
	}

	@Override
	public <T> List<T> filterResults(Object repository, QuerySpec querySpec, List<T> resources) {
		return resources;
	}
}
