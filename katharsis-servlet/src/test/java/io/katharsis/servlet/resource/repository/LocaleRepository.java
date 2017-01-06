package io.katharsis.servlet.resource.repository;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.servlet.resource.model.Locale;

public class LocaleRepository extends AbstractRepo<Locale, Long> {

	private static Map<Long, Locale> LOCALE_REPO = new ConcurrentHashMap<>();

	@Override
	protected Map<Long, Locale> getRepo() {
		return LOCALE_REPO;
	}
}
