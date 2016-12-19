package io.katharsis.jpa.internal.meta.impl;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaElement;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.utils.ClassUtils;

public class MetaAttributeImpl extends MetaElementImpl implements MetaAttribute {

	private String name;

	private Type type;

	private boolean association;

	private Method readMethod;

	private Method writeMethod;

	private Field field;

	public MetaAttributeImpl(MetaElement parent, String name, Type type) {
		super(parent);
		this.name = name;
		this.type = type;

		if (parent != null) {
			Class<?> beanClass = parent.asDataObject().getImplementationClass();
			this.readMethod = ClassUtils.findGetter(beanClass, name);
			this.writeMethod = ClassUtils.findSetter(beanClass, name, ClassUtils.getRawType(type));
			this.field = ClassUtils.findClassField(beanClass, name);
		}
	}

	public MetaAttributeImpl(MetaElement parent, PropertyDescriptor desc) {
		super(parent);
		this.name = desc.getName();
		this.type = desc.getReadMethod().getGenericReturnType();

		this.readMethod = desc.getReadMethod();
		this.writeMethod = desc.getWriteMethod();
		this.field = ClassUtils.findClassField(parent.asDataObject().getImplementationClass(), desc.getName());
	}

	@Override
	public MetaDataObject getParent() {
		return (MetaDataObject) super.getParent();
	}

	@Override
	public final boolean isAssociation() {
		return association;
	}

	public void setAssociation(boolean association) {
		this.association = association;
	}

	@Override
	public MetaType getType() {
		MetaElement meta = lookup.getMeta(type);
		return meta.asType();
	}

	@Override
	public Object getValue(Object dataObject) {
		PropertyUtilsBean utils = BeanUtilsBean.getInstance().getPropertyUtils();
		try {
			return utils.getNestedProperty(dataObject, getName());
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalStateException("cannot access field " + getName() + " for " + dataObject.getClass().getName(), e);
		}
	}

	@Override
	public void setValue(Object dataObject, Object value) {
		PropertyUtilsBean utils = BeanUtilsBean.getInstance().getPropertyUtils();
		try {
			utils.setNestedProperty(dataObject, getName(), value);
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalStateException("cannot access field " + getName() + " for " + dataObject.getClass().getName(), e);
		}
	}

	@Override
	public String getId() {
		return getParent().getId() + "." + getName();
	}

	@Override
	public MetaAttribute getOppositeAttribute() {
		return null;
	}

	@Override
	public boolean isDerived() {
		return false;
	}

	@Override
	public boolean isLazy() {
		return false;
	}

	@Override
	public boolean isVersion() {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addValue(Object dataObject, Object value) {
		Collection col = (Collection) getValue(dataObject);
		col.add(value);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void removeValue(Object dataObject, Object value) {
		Collection col = (Collection) getValue(dataObject);
		col.remove(value);
	}

	@Override
	public boolean isId() {
		return false;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> clazz) {
		T annotation = null;
		if (field != null) {
			annotation = field.getAnnotation(clazz);
		}
		if (annotation == null && readMethod != null) {
			annotation = readMethod.getAnnotation(clazz);
		}
		if (annotation == null && writeMethod != null) {
			annotation = writeMethod.getAnnotation(clazz);
		}
		return annotation;
	}

	@Override
	public Collection<Annotation> getAnnotations() {
		Collection<Annotation> annotations = new ArrayList<>();
		if (field != null) {
			annotations.addAll(Arrays.asList(field.getAnnotations()));
		}
		if (readMethod != null) {
			annotations.addAll(Arrays.asList(readMethod.getAnnotations()));
		}
		if (writeMethod != null) {
			annotations.addAll(Arrays.asList(writeMethod.getAnnotations()));
		}
		return annotations;
	}
}
