package io.katharsis.jpa.query.querydsl;

import io.katharsis.jpa.internal.query.backend.querydsl.QuerydslQueryImpl;

public interface QuerydslTranslationInterceptor { // NOSONAR not a functional interface

	<T> void intercept(QuerydslQueryImpl<T> query, QuerydslTranslationContext<T> translationContext);

}
