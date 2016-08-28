package io.katharsis.repository.base.query;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.resource.registry.ResourceRegistry;

public class DefaultQuerySpecParser implements QuerySpecParser {

	
	private ResourceRegistry resourceRegistry;

	public DefaultQuerySpecParser(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	@Override
	public QuerySpec fromParams(QueryParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryParams toParams(QuerySpec spec) {
		// TODO Auto-generated method stub
		return null;
	}
	
//
//	protected <T> void applyPaging(QueryExecutor<T> executor, QueryParams queryParams) {
//		// paging
//		int limit = -1;
//		int skip = 0;
//		Map<RestrictedPaginationKeys, Integer> pagination = queryParams.getPagination();
//		if (pagination != null) {
//			for (Map.Entry<RestrictedPaginationKeys, Integer> entry : pagination.entrySet()) {
//				RestrictedPaginationKeys key = entry.getKey();
//				if (key == RestrictedPaginationKeys.limit) {
//					limit = entry.getValue();
//				} else if (key == RestrictedPaginationKeys.offset) {
//					skip = entry.getValue();
//				} else {
//					throw new IllegalStateException("not supported: " + key);
//				}
//			}
//			executor.setWindow(skip, limit);
//		}
//	}
//
//	@Override
//	public <T> void prepareQuery(QueryBuilder<T> builder, QueryParams queryParams) {
//		applyGrouping(builder, queryParams);
//		applyIncludedFields(builder, queryParams);
//		applySorting(builder, queryParams);
//		applyFiltering(builder, queryParams);
//	}
//
//	protected <T> void applyIncludedFields(QueryBuilder<T> builder, QueryParams queryParams) {
//		TypedParams<IncludedFieldsParams> includedFields = queryParams.getIncludedFields();
//		if (includedFields != null && !includedFields.getParams().isEmpty()) {
//			throw new UnsupportedOperationException("selection of included fields not yet supported");
//		}
//	}
//
//	protected <T> void applyFiltering(QueryBuilder<T> builder, QueryParams queryParams) {
//		String resourceType = resourceRegistry.getResourceType(builder.getEntityClass());
//		MetaDataObject meta = MetaLookup.INSTANCE.getMeta(builder.getEntityClass()).asDataObject();
//
//		// filtering
//		TypedParams<FilterParams> filters = queryParams.getFilters();
//		if (filters != null && !filters.getParams().isEmpty()) {
//			FilterParams filterParams = filters.getParams().get(resourceType);
//
//			// no support to filter relations yet
//			if (filters.getParams().size() > 1 || filterParams == null)
//				throw new UnsupportedOperationException("only " + resourceType
//						+ " repositoryType supported for filtering, got " + filters.getParams().keySet());
//
//			for (Entry<String, Set<String>> entry : filterParams.getParams().entrySet()) {
//				String pathString = entry.getKey();
//				Set<String> stringValues = entry.getValue();
//
//				// find operation
//				FilterOperator op = FilterOperator.EQUAL;
//				for (FilterOperator availableOp : FilterOperator.values()) {
//					String suffix = "." + availableOp.toString().toLowerCase().replace("_", "");
//					if (pathString.toLowerCase().endsWith(suffix)) {
//						pathString = pathString.substring(0, pathString.length() - suffix.length());
//						op = availableOp;
//						break;
//					}
//				}
//
//				// convert value to proper type
//				// TODO introduce String parser API?
//				// FIXME move to TypeParser
//				MetaAttributePath path = meta.resolvePath(pathString);
//				MetaType filterType = path.getLast().getType();
//				Set<Object> typedValues = filterType.fromString(stringValues);
//
//				Object value = typedValues.size() == 1 ? typedValues.iterator().next() : typedValues;
//				builder.addFilter(pathString, op, value);
//			}
//		}
//	}
//
//	protected <T> void applySorting(QueryBuilder<T> builder, QueryParams queryParams) {
//		String resourceType = resourceRegistry.getResourceType(builder.getEntityClass());
//
//		// sorting
//		TypedParams<SortingParams> sorting = queryParams.getSorting();
//		if (sorting != null && !sorting.getParams().isEmpty()) {
//
//			SortingParams sortingParams = sorting.getParams().get(resourceType);
//
//			// no support to sort relations yet
//			if (sorting.getParams().size() > 1 || sortingParams == null)
//				throw new UnsupportedOperationException("only " + resourceType
//						+ " repositoryType supported for sorting, got " + sorting.getParams().keySet());
//
//			for (Map.Entry<String, RestrictedSortingValues> entry : sortingParams.getParams().entrySet()) {
//				String path = entry.getKey();
//				Direction dir = entry.getValue() == RestrictedSortingValues.asc ? Direction.ASC : Direction.DESC;
//				builder.addOrderBy(dir, path);
//			}
//		}
//	}
//
//	protected <T> void applyRelatedFields(QueryExecutor<T> executor, QueryParams queryParams) {
//		String resourceType = resourceRegistry.getResourceType(executor.getEntityClass());
//
//		// included relations
//		TypedParams<IncludedRelationsParams> includedRelations = queryParams.getIncludedRelations();
//		if (includedRelations != null && !includedRelations.getParams().isEmpty()) {
//			IncludedRelationsParams includedRelationsParams = includedRelations.getParams().get(resourceType);
//
//			// no support to include relations of relations yet (specify path
//			// from root instead)
//			if (includedRelations.getParams().size() > 1 || includedRelationsParams == null)
//				throw new UnsupportedOperationException(
//						"only " + resourceType + " repositoryType supported to include relations, got "
//								+ includedRelations.getParams().keySet());
//
//			for (Inclusion inclusion : includedRelationsParams.getParams()) {
//				String path = inclusion.getPath();
//				executor.fetch(path);
//			}
//		}
//
//	}
//
//	protected <T> void applyGrouping(QueryBuilder<T> builder, QueryParams queryParams) {
//		// grouping
//		TypedParams<GroupingParams> grouping = queryParams.getGrouping();
//		if (grouping != null && !grouping.getParams().isEmpty()) {
//			throw new UnsupportedOperationException("grouping not yet supported");
//		}
//	}

}
