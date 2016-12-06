package io.katharsis.jpa.internal.meta.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Type;
import java.util.Set;

import io.katharsis.jpa.internal.meta.MetaEmbeddable;
import io.katharsis.jpa.internal.meta.MetaKey;

public class MetaEmbeddableImpl extends MetaDataObjectImpl implements MetaEmbeddable {

  public MetaEmbeddableImpl(Class<?> implClass, Type implType, MetaDataObjectImpl superType) {
    super(implClass, implType, superType);
  }

  @Override
  protected MetaEmbeddableAttributeImpl newAttributeAttribute(MetaDataObjectImpl metaDataObject, PropertyDescriptor desc) {
    return new MetaEmbeddableAttributeImpl(this, desc);
  }

  @Override
  public MetaKey getPrimaryKey() {
    throw new IllegalStateException("not available");
  }

  @Override
  public Set<MetaKey> getKeys() {
    throw new IllegalStateException("not available");
  }

  @Override
  public Object fromString(String value) {
    throw new UnsupportedOperationException("no yet implemented");
  }
}
