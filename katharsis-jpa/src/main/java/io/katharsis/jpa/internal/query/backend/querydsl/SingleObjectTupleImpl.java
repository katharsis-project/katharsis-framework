package io.katharsis.jpa.internal.query.backend.querydsl;

import java.util.List;

import javax.persistence.TupleElement;

import com.querydsl.core.types.Expression;

import io.katharsis.jpa.query.criteria.JpaCriteriaTuple;
import io.katharsis.jpa.query.querydsl.QuerydslTuple;

public class SingleObjectTupleImpl implements QuerydslTuple, JpaCriteriaTuple {

	private Object entity;

	public SingleObjectTupleImpl(Object entity) {
		this.entity = entity;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(int index, Class<T> type) {
		if (index == 0) {
			return (T) entity;
		}
		else {
			throw new IndexOutOfBoundsException("index=" + index);
		}
	}

	@Override
	public <T> T get(Expression<T> expr) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public Object[] toArray() {
		return new Object[] { entity };
	}

	@Override
	public <T> T get(String name, Class<T> clazz) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <X> X get(TupleElement<X> element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(int index) {
		return get(index, Object.class);
	}

	@Override
	public List<TupleElement<?>> getElements() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reduce(int numEntriesToIgnore) {
		throw new UnsupportedOperationException();
	}

}
