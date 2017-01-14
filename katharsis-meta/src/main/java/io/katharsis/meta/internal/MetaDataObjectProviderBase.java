package io.katharsis.meta.internal;

import java.beans.PropertyDescriptor;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.provider.MetaProviderBase;

public abstract class MetaDataObjectProviderBase<T extends MetaDataObject> extends MetaProviderBase {

	protected void createAttributes(T meta) {
		Class<?> implClass = meta.getImplementationClass();
		PropertyUtilsBean utils = BeanUtilsBean.getInstance().getPropertyUtils();
		PropertyDescriptor[] descriptors = utils.getPropertyDescriptors(implClass);
		for (PropertyDescriptor desc : descriptors) {
			if (desc.getReadMethod().getDeclaringClass() != implClass)
				continue; // contained in super type

			createAttribute(meta, desc);
		}
		
		// 
	}

	protected MetaAttribute createAttribute(T metaDataObject, PropertyDescriptor desc) {
		MetaAttribute attr = new MetaAttribute();
		attr.setName(desc.getName());
		attr.setParent(metaDataObject);
		return attr;
	}
}
