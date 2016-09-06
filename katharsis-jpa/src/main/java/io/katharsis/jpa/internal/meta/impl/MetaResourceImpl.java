package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformation;

public class MetaResourceImpl extends MetaDataObjectImpl implements MetaEntity {

	public MetaResourceImpl(Class<?> implClass, Type implType, MetaDataObjectImpl superType) {
		super(implClass, implType, superType);

	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void initAttributes() {
		Class<?> implClass = this.getImplementationClass();
		AnnotationResourceInformationBuilder builder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		ResourceInformation resourceInformation = builder.build(implClass);

		ResourceField idField = resourceInformation.getIdField();
		MetaAttributeImpl idAttr = new MetaAttributeImpl(this, idField.getUnderlyingName(), idField.getGenericType());
		addAttribute(idAttr);
		MetaKey key = new MetaKeyImpl(this, idAttr.getName(), (List) Arrays.asList(idAttr), true, true, idAttr.getType());
		setPrimaryKey(key);

		Set<ResourceField> attrFields = resourceInformation.getAttributeFields().getFields();
		for (ResourceField field : attrFields) {
			MetaAttributeImpl attr = new MetaAttributeImpl(this, field.getUnderlyingName(), field.getGenericType());
			addAttribute(attr);
		}

		for (ResourceField field : resourceInformation.getRelationshipFields()) {
			MetaAttributeImpl attr = new MetaAttributeImpl(this, field.getUnderlyingName(), field.getGenericType());
			attr.setAssociation(true);
			addAttribute(attr);
		}
	}
}