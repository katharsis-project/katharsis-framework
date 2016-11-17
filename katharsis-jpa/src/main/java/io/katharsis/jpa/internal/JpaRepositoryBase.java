package io.katharsis.jpa.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import io.katharsis.jpa.JpaModule;
import io.katharsis.jpa.JpaRepositoryConfig;
import io.katharsis.jpa.JpaRepositoryFilter;
import io.katharsis.jpa.mapping.JpaMapper;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.jpa.query.Tuple;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.resource.list.ResourceList;

public abstract class JpaRepositoryBase<T> {

	protected JpaModule module;

	protected JpaRepositoryConfig<T> repositoryConfig;

	protected <E> JpaRepositoryBase(JpaModule module, JpaRepositoryConfig<T> repositoryConfig) {
		this.module = module;
		this.repositoryConfig = repositoryConfig;
	}

	/**
	 * By default LookupIncludeBehavior.ALWAYS is in place and we let the relationship repositories load the relations. There 
	 * is no need to do join fetches, which can lead to problems with paging (evaluated in memory instead of the db).
	 * 
	 * @param fieldName of the relation to fetch
	 * @return relation will be eagerly fetched if true
	 */
	protected boolean fetchRelations(String fieldName) { // NOSONAR
		return false;
	}

	/**
	 * For read we always use a clean entity manager and return detached entities.
	 * For example, Jackson cannot handle proxies. And Katharsis is modifying the entities
	 * for subgraph loading.
	 */
	protected void resetEntityManager() {
		EntityManager em = module.getEntityManager();
		em.flush();
		em.clear();
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

	protected static <D, I> Map<I, D> getUniqueOrNull(Map<I, Iterable<D>> map) {
		Map<I, D> oneMap = new HashMap<>();
		for (Map.Entry<I, Iterable<D>> entry : map.entrySet()) {
			I id = entry.getKey();
			Iterable<D> values = entry.getValue();

			Iterator<D> iterator = values.iterator();
			if (iterator.hasNext()) {
				D value = iterator.next();
				if (iterator.hasNext()) {
					throw new IllegalStateException("expected unique result for " + id);
				}
				oneMap.put(id, value);
			}
		}
		return oneMap;
	}

	protected QuerySpec filterQuerySpec(QuerySpec querySpec) {
		QuerySpec filteredQuerySpec = querySpec;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredQuerySpec = filter.filterQuerySpec(this, filteredQuerySpec);
			}
		}
		return filteredQuerySpec;
	}

	protected <E> JpaQuery<E> filterQuery(QuerySpec querySpec, JpaQuery<E> query) {
		JpaQuery<E> filteredQuery = query;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredQuery = filter.filterQuery(this, querySpec, filteredQuery);
			}
		}
		return filteredQuery;
	}

	protected <E> JpaQueryExecutor<E> filterExecutor(QuerySpec querySpec, JpaQueryExecutor<E> executor) {
		JpaQueryExecutor<E> filteredExecutor = executor;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredExecutor = filter.filterExecutor(this, querySpec, filteredExecutor);
			}
		}
		return filteredExecutor;
	}

	protected List<Tuple> filterTuples(QuerySpec querySpec, List<Tuple> tuples) {
		List<Tuple> filteredTuples = tuples;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredTuples = filter.filterTuples(this, querySpec, filteredTuples);
			}
		}
		return filteredTuples;
	}

	protected ResourceList<T> filterResults(QuerySpec querySpec, ResourceList<T> resources) {
		ResourceList<T> filteredResources = resources;
		for (JpaRepositoryFilter filter : module.getFilters()) {
			if (filter.accept(repositoryConfig.getResourceClass())) {
				filteredResources = filter.filterResults(this, querySpec, filteredResources);
			}
		}
		return filteredResources;
	}

	protected ResourceList<T> map(List<Tuple> tuples) {
		ResourceList<T> resources = repositoryConfig.newResultList();
		for (Tuple tuple : tuples) {
			JpaMapper<Object, T> mapper = repositoryConfig.getMapper();
			resources.add(mapper.map(tuple));
		}
		return resources;
	}
}
