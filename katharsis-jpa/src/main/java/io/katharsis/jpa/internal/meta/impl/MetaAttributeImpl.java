package io.katharsis.jpa.internal.meta.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.meta.MetaType;

public abstract class MetaAttributeImpl extends MetaElementImpl implements MetaAttribute {

	protected PropertyDescriptor desc;

	public MetaAttributeImpl(MetaElementImpl parent, PropertyDescriptor desc) {
		super(parent);
		this.desc = desc;
	}
	
	@Override
	public MetaDataObject getParent(){
		return (MetaDataObject) super.getParent();
	}

	public abstract boolean isAssociation();

	@Override
	public MetaType getType() {
		return MetaLookup.INSTANCE.getMeta(desc.getReadMethod().getGenericReturnType()).asType();
	}

	public Object getValue(Object dataObject) {
		PropertyUtilsBean utils = BeanUtilsBean.getInstance().getPropertyUtils();
		try {
			return utils.getNestedProperty(dataObject, getName());
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalStateException("cannot access field " + getName() + " for " + dataObject.getClass().getName());
		}
	}

	public void setValue(Object dataObject, Object value) {
		PropertyUtilsBean utils = BeanUtilsBean.getInstance().getPropertyUtils();
		try {
			utils.setNestedProperty(dataObject, getName(), value);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalStateException("cannot access field " + getName() + " for " + dataObject.getClass().getName());
		}
	}

}
