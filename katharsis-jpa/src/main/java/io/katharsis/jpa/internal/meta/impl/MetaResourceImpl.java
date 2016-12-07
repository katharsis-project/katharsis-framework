package io.katharsis.jpa.internal.meta.impl;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformation;

public class MetaResourceImpl extends MetaDataObjectImpl {

	private MetaAttributeImpl idAttr;

	public MetaResourceImpl(Class<?> implClass, Type implType, MetaDataObjectImpl superType) {
		super(implClass, implType, superType);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void init(MetaLookup lookup) {
		super.init(lookup);

		MetaKey key = new MetaKeyImpl(this, idAttr.getName(), (List) Arrays.asList(idAttr), true, idAttr.getType());
		setPrimaryKey(key);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	protected void initAttributes() {
		Class<?> implClass = this.getImplementationClass();
		AnnotationResourceInformationBuilder builder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		ResourceInformation resourceInformation = builder.build(implClass);

		ResourceField idField = resourceInformation.getIdField();
		idAttr = new MetaAttributeImpl(this, idField.getUnderlyingName(), idField.getGenericType());
		addAttribute(idAttr);

		super.init(lookup);

		List<ResourceField> attrFields = resourceInformation.getAttributeFields().getFields();
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
