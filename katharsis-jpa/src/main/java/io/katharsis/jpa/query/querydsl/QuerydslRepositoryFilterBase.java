package io.katharsis.jpa.query.querydsl;

import io.katharsis.jpa.JpaRepositoryFilterBase;
import io.katharsis.queryspec.QuerySpec;

public class QuerydslRepositoryFilterBase extends JpaRepositoryFilterBase implements QuerydslRepositoryFilter {

	@Override
	public <T> void filterQueryTranslation(Object repository, QuerySpec querySpec,
			QuerydslTranslationContext<T> translationContext) {
		// nothing to do
	}
}
