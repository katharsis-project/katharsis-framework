package io.katharsis.servlet.resource.repository;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.servlet.resource.model.Locale;

/**
 * Created by nickmitchell on 1/6/17.
 */
public class LocaleRepository implements ResourceRepository<Locale, Long> {

	private static Map<Long, Locale> LOCALE_REPO = new ConcurrentHashMap<>();

	@Override
	public Locale findOne(Long id, QueryParams queryParams) {
		return LOCALE_REPO.get(id);
	}

	@Override
	public Iterable<Locale> findAll(QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<Locale> findAll(Iterable<Long> longs, QueryParams queryParams) {
		return LOCALE_REPO.values();
	}

	@Override
	public <S extends Locale> S save(S entity) {
		return (S) LOCALE_REPO.put(entity.getId(), entity);
	}

	@Override
	public void delete(Long id) {
		LOCALE_REPO.remove(id);
	}
}
