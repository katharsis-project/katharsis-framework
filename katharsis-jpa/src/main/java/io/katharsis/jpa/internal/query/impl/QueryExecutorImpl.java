package io.katharsis.jpa.internal.query.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Subgraph;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.query.QueryExecutor;
import io.katharsis.utils.StringUtils;

public class QueryExecutorImpl<T> implements QueryExecutor<T> {

	private CriteriaQuery<T> query;

	private int skip = 0;
	private int limit = -1;

	private boolean cached = false;
	private EntityManager em;
	private int numAutoSelections;

	private Set<MetaAttributePath> fetchPaths = new HashSet<MetaAttributePath>();

	private MetaDataObject meta;

	public QueryExecutorImpl(EntityManager em, MetaDataObject meta, CriteriaQuery<T> criteriaQuery, int numAutoSelections) {
		this.em = em;
		this.meta = meta;
		this.query = criteriaQuery;
		this.numAutoSelections = numAutoSelections;
	}

	@Override
	public QueryExecutor<T> fetch(String... fetchPath) {
		// include path an all prefix paths
		MetaAttributePath path = this.meta.resolvePath(StringUtils.join(MetaAttributePath.PATH_SEPARATOR, Arrays.asList(fetchPath)));
		for (int i = 1; i <= path.length(); i++) {
			fetchPaths.add(path.subPath(0, i));
		}
		return this;
	}

	@Override
	public QueryExecutor<T> setCached(boolean cached) {
		this.cached = cached;
		return this;
	}

	@Override
	public QueryExecutor<T> setWindow(int skip, int limit) {
		this.skip = skip;
		this.limit = limit;
		return this;
	}

	@Override
	public CriteriaQuery<T> getQuery() {
		return query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getResultList() {
		List<?> list = executeQuery();
		// due to sorting & distinct we have a multiselect even
		// if we are only interested in the entites.
		List<T> resultList;
		if (query.getSelection().isCompoundSelection()) {
			List<T> entityList = new ArrayList<T>();
			for (Object obj : list) {
				Object[] values = (Object[]) obj;
				entityList.add((T) values[0]);
			}
			resultList = entityList;
		} else {
			resultList = (List<T>) list;
		}
		return resultList;
	}

	protected List<?> executeQuery() {
		return executeQuery(query);
	}

	@Override
	public T getUniqueResult(boolean nullable) {
		List<T> list = getResultList();
		if (list.size() > 1)
			throw new IllegalStateException("query does not return unique value, " + list.size() + " results returned");
		if (!list.isEmpty())
			return list.get(0);
		else if (nullable)
			return null;
		else
			throw new IllegalStateException("no result found");
	}

	protected List<?> executeQuery(CriteriaQuery<?> criteriaQuery) {
		TypedQuery<?> typedQuery = null;
		List<?> resultList = null;
		try {
			typedQuery = em.createQuery(criteriaQuery);

			// apply graph control
			applyFetchPaths(typedQuery);

			// control Hibernate query caching
			if (cached) {
				typedQuery.setHint("org.hibernate.cacheable", Boolean.TRUE);
			}

			if (limit > 0) {
				typedQuery.setMaxResults(limit);
			}
			typedQuery.setFirstResult(skip);

			// query execution
			resultList = typedQuery.getResultList();

			// post processing (distinct and tuples => views)
			if (criteriaQuery.getSelection().isCompoundSelection() && criteriaQuery.isDistinct() && QueryUtil.hasManyRootsFetchesOrJoins(criteriaQuery)) {
				resultList = enforceDistinct(resultList);
			}

			if (numAutoSelections > 0) {
				resultList = truncateTuples(resultList, numAutoSelections);
			}

			return resultList;
		} finally {
			// TODO remo: removeDistinct(criteriaQuery);
		}
	}

	protected void applyFetchPaths(TypedQuery<?> criteriaQuery) {
		EntityGraph<T> graph = em.createEntityGraph(getEntityClass());
		for (MetaAttributePath fetchPath : fetchPaths) {
			applyFetchPaths(graph, fetchPath);
		}
		criteriaQuery.setHint("javax.persistence.fetchgraph", graph);
	}

	private Subgraph<Object> applyFetchPaths(EntityGraph<T> graph, MetaAttributePath fetchPath) {
		if (fetchPath.length() >= 2) {
			// ensure parent is fetched
			MetaAttributePath parentPath = fetchPath.subPath(0, fetchPath.length() - 1);
			Subgraph<Object> parentGraph = applyFetchPaths(graph, parentPath);
			return parentGraph.addSubgraph(fetchPath.toString());
		} else {
			return graph.addSubgraph(fetchPath.toString());
		}
	}

	// TODO remove this by a property Tuple object (and Object[] in general...)
	private List<?> truncateTuples(List<?> list, int numToRemove) {
		ArrayList<Object> truncatedList = new ArrayList<Object>();
		for (Object obj : list) {
			Object[] tuple = (Object[]) obj;
			Object[] truncatedTuple = new Object[tuple.length - numToRemove];
			System.arraycopy(tuple, 0, truncatedTuple, 0, truncatedTuple.length);
			truncatedList.add(truncatedTuple);
		}
		return truncatedList;
	}

	// @SuppressWarnings({ "unchecked" })
	// @Override
	// public List<Object> getIdResultList() {
	// // get primary key
	// IMetaDataObject entityMeta = meta;
	// if (entityMeta instanceof IMetaProjection) {
	// entityMeta = ((IMetaProjection) entityMeta).getBaseType();
	// }
	// IMetaKey primaryKey = entityMeta.getPrimaryKey();
	// List<IMetaAttribute> elements = primaryKey.getElements();
	// if (elements.size() != 1)
	// throw new IllegalStateException("compound keys not implemented: " + primaryKey);
	// IMetaAttribute primaryKeyAttr = elements.get(0);
	// Class<?> primaryKeyType = primaryKeyAttr.getType().getImplementationClass();
	//
	// // clone query and only retrieve ids
	// CriteriaBuilder builder = em.getCriteriaBuilder();
	// CriteriaQuery<?> idsQuery = builder.createQuery(primaryKeyType);
	//
	// CriteriaUtil.copyCriteria(builder, query, idsQuery, true, false, null);
	//
	// idsQuery.select((Selection) getFromRoot().get(primaryKeyAttr.getName()));
	//
	// return (List<Object>) executeQuery(idsQuery);
	// }

	/**
	 * Returns the row count for the CriteriaQuery. This will not work for GROUP BY queries.
	 */
	@Override
	public long getTotalRowCount() {

		CriteriaBuilder builder = em.getCriteriaBuilder();

		throw new RuntimeException("not supported");

		//
		// CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		// CriteriaUtil.copyCriteria(builder, criteriaQuery, countQuery, false, false, null);
		// countQuery.distinct(false);
		//
		// Root<?> root = countQuery.getRoots().iterator().next();
		// Expression<?> expr = root;
		// if (criteriaQuery.isDistinct()) {
		// Map<String, Object> properties = em.getProperties();
		// Class<?> rootType = expr.getJavaType();
		// if (IReadOnlyDataObject.class.isAssignableFrom(rootType)) {
		// IMetaDataObject metaData = DaoUtil.getMetaData(rootType);
		// List<IMetaAttribute> pkElements = metaData.getPrimaryKey().getElements();
		// if (pkElements.size() == 1 && "Shared.UID".equals(pkElements.get(0).getType().getQualifiedName())) {
		// Path<?> uid = root.get(pkElements.get(0).getName());
		// Path<?> domainId = uid.get("domainID");
		// Path<?> ipid = uid.get("ipid");
		// // expr = (Expression<?>) builder.construct(pkElements.get(0).getType().getImplementationClass(),
		// // domainId, ipid);
		// // expr = builder.concat((Path<String>)domainId, (Path<String>)ipid);
		// expr = ipid;
		// }
		// }
		// countQuery.select(builder.countDistinct(expr));
		// } else
		// countQuery.select(builder.count(expr));
		// TypedQuery<Long> typedCountQuery = em.createQuery(countQuery);
		//
		// TimeTaker tt = new TimeTaker();
		// long result = -1;
		// Throwable ex = null;
		// try {
		// result = typedCountQuery.getSingleResult();
		// return result;
		// } catch (RuntimeException e) {
		// ex = e;
		// throw e;
		// } finally {
		// DaoLoggerUtil.debugQuery(meta.getImplementationClass(), typedCountQuery, null, tt, result, ex);
		// }
	}

	static class Tuple {
		private Object[] data;
		private int hashCode;

		Tuple(Object[] data) {
			this.data = data;
			this.hashCode = Arrays.hashCode(data);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Tuple))
				return false;
			Tuple tuple = (Tuple) obj;
			return tuple.hashCode == hashCode && Arrays.equals(data, tuple.data);
		}
	}

	private static List<?> enforceDistinct(List<?> list) {
		HashSet<Tuple> distinctSet = new HashSet<Tuple>();
		ArrayList<Object[]> distinctResult = new ArrayList<Object[]>();
		boolean hasDuplicates = false;
		for (Object obj : list) {
			Object[] values = (Object[]) obj;
			Tuple tuple = new Tuple(values);
			if (!distinctSet.contains(tuple)) {
				distinctSet.add(tuple);
				distinctResult.add(values);
			} else {
				hasDuplicates = true;
			}
		}

		// TODO remo
		// if (hasDuplicates) {
		// if ((fetchCriteria.getSkip() != 0 || fetchCriteria.getNum() != 0) && logger.isWarnEnabled()) {
		// // Hibernate does not enforce distinct after evaluating fetches
		// // for tuple queries
		// // TODO maybe be could use the Hibernate specific FetchType.SUBQUERY to prevent this...
		// logger.warn("paging combined with FetchJoins on many relation will not work due to 'broken' distinct
		// (Hibernatte/JPA limitation)");
		// }
		// }

		return distinctResult;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getEntityClass() {
		return (Class<T>) meta.getImplementationClass();
	}

}
