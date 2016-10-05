package io.katharsis.jpa.internal;

import java.util.ArrayList;
import java.util.List;

import io.katharsis.jpa.JpaModule;
import io.katharsis.jpa.JpaRepositoryFilter;
import io.katharsis.jpa.internal.paging.PagedRepositoryBase;
import io.katharsis.jpa.mapping.JpaMapper;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.Tuple;
import io.katharsis.queryspec.QuerySpec;

public abstract class JpaRepositoryBase<T> extends PagedRepositoryBase<T> {

	protected JpaModule module;

	protected Class<T> resourceClass;

	protected JpaMapper<?, T> mapper;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <E> JpaRepositoryBase(JpaModule module, Class<T> resourceType, JpaMapper mapper) {
		this.module = module;
		this.resourceClass = resourceType;
		this.mapper = mapper;
	}

	protected static <D> D getUniqueOrNull(List<D> list) {
		if (list.isEmpty()) {
			return null;
		}
		else if (list.size() == 1) {
			return list.get(0);
		}
		else {
			throw new IllegalStateException("unique result expected");
		}
	}

	protected QuerySpec filterQuerySpec(QuerySpec querySpec) {
		QuerySpec filteredQuerySpec = querySpec;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(resourceClass)) {
				filteredQuerySpec = filter.filterQuerySpec(this, filteredQuerySpec);
			}
		}
		return filteredQuerySpec;
	}

	protected <E> JpaQuery<E> filterQuery(QuerySpec querySpec, JpaQuery<E> query) {
		JpaQuery<E> filteredQuery = query;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(resourceClass)) {
				filteredQuery = filter.filterQuery(this, querySpec, filteredQuery);
			}
		}
		return filteredQuery;
	}

	protected <E> JpaQueryExecutor<E> filterExecutor(QuerySpec querySpec, JpaQueryExecutor<E> executor) {
		JpaQueryExecutor<E> filteredExecutor = executor;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(resourceClass)) {
				filteredExecutor = filter.filterExecutor(this, querySpec, filteredExecutor);
			}
		}
		return filteredExecutor;
	}

	protected List<Tuple> filterTuples(QuerySpec querySpec, List<Tuple> tuples) {
		List<Tuple> filteredTuples = tuples;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(resourceClass)) {
				filteredTuples = filter.filterTuples(this, querySpec, filteredTuples);
			}
		}
		return filteredTuples;
	}

	protected List<T> filterResults(QuerySpec querySpec, List<T> resources) {
		List<T> filteredResources = resources;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(resourceClass)) {
				filteredResources = filter.filterResults(this, querySpec, filteredResources);
			}
		}
		return filteredResources;
	}

	protected List<T> map(List<Tuple> tuples) {
		List<T> resources = new ArrayList<>();
		for (Tuple tuple : tuples) {
			resources.add(mapper.map(tuple));
		}
		return resources;
	}
}
