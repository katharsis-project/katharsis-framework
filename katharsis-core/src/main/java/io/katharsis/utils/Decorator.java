package io.katharsis.utils;

/**
 * Generic interface for decorates decorating an other object.
 * 
 * @param <T> type
 */
public interface Decorator<T> {

	public void setDecoratedObject(T object);
}
