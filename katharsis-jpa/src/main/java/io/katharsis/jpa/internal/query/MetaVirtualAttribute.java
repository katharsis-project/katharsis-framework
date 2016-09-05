package io.katharsis.jpa.internal.query;

import java.lang.reflect.Type;

import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.impl.MetaAttributeImpl;

public class MetaVirtualAttribute extends MetaAttributeImpl {

	private MetaDataObject virtualParent;

	public MetaVirtualAttribute(MetaDataObject parent, String name, Type type) {
		super(null, name, type);

		// not attach to actual parent!
		this.virtualParent = parent;
	}

	@Override
	public MetaDataObject getParent() {
		return virtualParent;
	}

	@Override
	public Object getValue(Object dataObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValue(Object dataObject, Object value) {
		throw new UnsupportedOperationException();
	}
}
