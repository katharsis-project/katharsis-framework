package io.katharsis.jpa.query.querydsl;

import io.katharsis.jpa.JpaRepositoryFilter;
import io.katharsis.queryspec.QuerySpec;

public interface QuerydslRepositoryFilter extends JpaRepositoryFilter {

	/**
	 * Allows to hook into the translation of the generic query into a querydsl query.
	 * 
	 * @param repository invoked
	 * @param querySpec provided by caller
	 * @param translationContext to modify the translation
	 * @return filtered query
	 */
	public <T> void filterQueryTranslation(Object repository, QuerySpec querySpec,
			QuerydslTranslationContext<T> translationContext);
}
