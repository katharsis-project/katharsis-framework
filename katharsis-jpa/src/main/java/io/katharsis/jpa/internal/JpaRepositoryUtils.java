package io.katharsis.jpa.internal;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.query.JpaQuery;
import io.katharsis.jpa.query.JpaQueryExecutor;
import io.katharsis.queryspec.FilterSpec;
import io.katharsis.queryspec.IncludeSpec;
import io.katharsis.queryspec.PagingSpec;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.SortSpec;
import io.katharsis.utils.PreconditionUtil;

import java.util.Arrays;
import java.util.Set;

public class JpaRepositoryUtils {

	private JpaRepositoryUtils() {
	}

	/**
	 * Gets the primary key attribute of the given entity. Assumes a primary key
	 * is available and no compound primary keys are supported.
	 */
	public static MetaAttribute getPrimaryKeyAttr(MetaEntity meta) {
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
		if (querySpec.getPagingSpec() == null)
			return;
		PagingSpec pagingSpec = querySpec.getPagingSpec();
		if (pagingSpec.getOffset() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("offset cannot be larger than Integer.MAX_VALUE");
		}
		executor.setOffset((int) pagingSpec.getOffset());
		if (pagingSpec.getLimit() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("limit cannot be larger than Integer.MAX_VALUE");
		}
		executor.setLimit((int) pagingSpec.getLimit());
	}
}
