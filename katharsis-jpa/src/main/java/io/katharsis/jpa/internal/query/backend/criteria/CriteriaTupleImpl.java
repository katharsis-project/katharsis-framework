package io.katharsis.jpa.internal.query.backend.criteria;

import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

public class CriteriaTupleImpl implements Tuple, io.katharsis.jpa.query.Tuple {

	private Object[] data;

	private Map<String, Integer> selectionBindings;

	protected CriteriaTupleImpl(Object[] data, Map<String, Integer> selectionBindings) {
		this.data = data;
		this.selectionBindings = selectionBindings;
	}

	@Override
	public <X> X get(TupleElement<X> tupleElement) {
		throw new UnsupportedOperationException("not implemented");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> X get(String alias, Class<X> type) {
		return (X) get(alias);
	}

	@Override
	public Object get(String alias) {
		Integer index = selectionBindings.get(alias);
		if (index == null) {
			throw new IllegalArgumentException("selection " + alias + " not found");
		}
		return get(index.intValue());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> X get(int i, Class<X> type) {
		return (X) data[i];
	}

	@Override
	public Object get(int i) {
		return data[i];
	}

	@Override
	public Object[] toArray() {
		return data;
	}

	@Override
	public List<TupleElement<?>> getElements() {
		throw new UnsupportedOperationException("not implemented");
	}
}
