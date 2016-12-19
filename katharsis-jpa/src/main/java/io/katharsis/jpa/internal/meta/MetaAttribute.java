package io.katharsis.jpa.internal.meta;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface MetaAttribute extends MetaTypedElement {

	public MetaAttribute getOppositeAttribute();

	@Override
	MetaType getType();

	Object getValue(Object dataObject);

	void setValue(Object dataObject, Object value);

	boolean isAssociation();

	boolean isDerived();

	@Override
	MetaDataObject getParent();

	void addValue(Object dataObject, Object value);

	void removeValue(Object dataObject, Object value);

	public boolean isLazy();

	public boolean isVersion();

	public boolean isId();

	public Collection<Annotation> getAnnotations();

	public <T extends Annotation> T getAnnotation(Class<T> clazz);

}
