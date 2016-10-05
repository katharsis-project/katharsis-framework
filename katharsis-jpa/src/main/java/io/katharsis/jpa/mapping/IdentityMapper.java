package io.katharsis.jpa.mapping;

import io.katharsis.jpa.query.Tuple;

public class IdentityMapper<E> implements JpaMapper<E, E> {

	private IdentityMapper() {
	}

	public static final <E> IdentityMapper<E> newInstance() {
		return new IdentityMapper<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E map(Tuple tuple) {
		return (E) tuple.get(0, Object.class);
	}

	@Override
	public E unmap(E dto) {
		return dto;
	}
}
