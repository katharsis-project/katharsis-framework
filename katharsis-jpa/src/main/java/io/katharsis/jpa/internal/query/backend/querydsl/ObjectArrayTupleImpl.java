package io.katharsis.jpa.internal.query.backend.querydsl;

import java.util.Arrays;
import java.util.List;

import javax.persistence.TupleElement;

import com.querydsl.core.types.Expression;

import io.katharsis.jpa.query.criteria.JpaCriteriaTuple;
import io.katharsis.jpa.query.querydsl.QuerydslTuple;

public class ObjectArrayTupleImpl implements QuerydslTuple, JpaCriteriaTuple {

	private Object[] data;

	private int numEntriesToIgnore;

	public ObjectArrayTupleImpl(Object entity) {
		if (entity instanceof Object[]) {
			data = (Object[]) entity;
		}
		else {
			data = new Object[] { entity };
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(int index, Class<T> type) {
		return (T) data[index + numEntriesToIgnore];
	}

	@Override
	public <T> T get(Expression<T> expr) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return data.length - numEntriesToIgnore;
	}

	@Override
	public Object[] toArray() {
		if (numEntriesToIgnore > 0) {
			return Arrays.copyOfRange(data, numEntriesToIgnore, data.length);
		}
		else {
			return data;
		}
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
		this.numEntriesToIgnore = numEntriesToIgnore;
	}

}
