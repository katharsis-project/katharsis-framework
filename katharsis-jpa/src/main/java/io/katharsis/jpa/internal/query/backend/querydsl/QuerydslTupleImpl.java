package io.katharsis.jpa.internal.query.backend.querydsl;

import java.util.Map;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;

import io.katharsis.jpa.query.querydsl.QuerydslTuple;

public class QuerydslTupleImpl implements QuerydslTuple {

	private Tuple tuple;

	private Map<String, Integer> selectionBindings;

	public QuerydslTupleImpl(Tuple tuple, Map<String, Integer> selectionBindings) {
		this.tuple = tuple;
		this.selectionBindings = selectionBindings;
	}

	@Override
	public <T> T get(int index, Class<T> type) {
		return tuple.get(index, type);
	}

	@Override
	public <T> T get(Expression<T> expr) {
		return tuple.get(expr);
	}

	@Override
	public int size() {
		return tuple.size();
	}

	@Override
	public Object[] toArray() {
		return tuple.toArray();
	}

	@Override
	public <T> T get(String name, Class<T> clazz) {
		Integer index = selectionBindings.get(name);
		if (index == null) {
			throw new IllegalArgumentException("selection " + name + " not found");
		}
		return get(index.intValue(), clazz);
	}

}
