package io.katharsis.jpa.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Set;

import io.katharsis.jpa.annotations.JpaMergeRelations;
import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.query.AbstractQueryExecutorImpl;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.IncludeSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.utils.PreconditionUtil;

public class JpaRepositoryUtils {

	private JpaRepositoryUtils() {
	}

	/**
	 * @param meta of the entity
	 * @return Gets the primary key attribute of the given entity. Assumes a primary key
	 * is available and no compound primary keys are supported.
	 */
	public static MetaAttribute getPrimaryKeyAttr(MetaDataObject meta) {
		MetaKey primaryKey = meta.getPrimaryKey();
		PreconditionUtil.assertNotNull("no primary key", primaryKey);
		PreconditionUtil.assertEquals("non-compound primary key expected", 1, primaryKey.getElements().size());
		return primaryKey.getElements().get(0);
	}

	public static void prepareQuery(JpaQuery<?> query, QuerySpec querySpec, Set<String> computedAttrs) {

		for (String computedAttr : computedAttrs) {
			query.addSelection(Arrays.asList(computedAttr));
		}

		for (FilterSpec filter : querySpec.getFilters()) {
			query.addFilter(filter);
		}
		for (SortSpec sortSpec : querySpec.getSort()) {
			query.addSortBy(sortSpec);
		}
		if (!querySpec.getIncludedFields().isEmpty()) {
			throw new UnsupportedOperationException("includeFields not yet supported");
		}

	}

	public static void prepareExecutor(JpaQueryExecutor<?> executor, QuerySpec querySpec, boolean includeRelations) {
		if (includeRelations) {
			for (IncludeSpec included : querySpec.getIncludedRelations()) {
				executor.fetch(included.getAttributePath());
			}
		}
		executor.setOffset((int) querySpec.getOffset());
		if (querySpec.getOffset() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("offset cannot be larger than Integer.MAX_VALUE");
		}
		if (querySpec.getLimit() != null) {
			if (querySpec.getLimit() > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("limit cannot be larger than Integer.MAX_VALUE");
			}
			executor.setLimit((int) querySpec.getLimit().longValue());
		}

		addMergeInclusions(executor, querySpec);

	}

	/**
	 * related attribute that are merged into a resource should be loaded by graph control 
	 * to avoid lazy-loading or potential lack of session in serialization.
	 */
	private static void addMergeInclusions(JpaQueryExecutor<?> executor, QuerySpec querySpec) {
		ArrayDeque<String> attributePath = new ArrayDeque<>();
		Class<?> resourceClass = querySpec.getResourceClass();
		
		MetaDataObject entityMeta = ((AbstractQueryExecutorImpl<?>) executor).getMeta();
		MetaLookup lookup = entityMeta.getLookup();
		MetaDataObject resourceMeta = lookup.getMeta(resourceClass).asDataObject();
		
		addMergeInclusions(attributePath, executor, resourceMeta);
	}

	private static void addMergeInclusions(Deque<String> attributePath, JpaQueryExecutor<?> executor, MetaDataObject meta) {
		Class<?> resourceClass = meta.getImplementationClass();

		JpaMergeRelations annotation = resourceClass.getAnnotation(JpaMergeRelations.class);
		if (annotation != null) {
			for (String attrName : annotation.attributes()) {
				attributePath.push(attrName);
				executor.fetch(new ArrayList<>(attributePath));

				// recurse
				MetaAttribute attr = meta.getAttribute(attrName);
				MetaDataObject attrType = attr.getType().getElementType().asDataObject();
				addMergeInclusions(attributePath, executor, attrType);

				attributePath.pop();
			}
		}
	}

}
