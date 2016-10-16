package io.katharsis.jpa.mapping;

public interface JpaMapping<E, D> {

	public Class<E> getEntityClass();

	public Class<D> getDtoClass();

	public JpaMapper<E, D> getMapper();
}
