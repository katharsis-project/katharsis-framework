package io.katharsis.core.internal.utils;

/**
 * Used to convert generic json meta and links information to typed ones.
 * 
 * @param <T> type of information
 */
public interface CastableInformation<T> {

	public <L extends T> L as(Class<L> linksClass);
}
