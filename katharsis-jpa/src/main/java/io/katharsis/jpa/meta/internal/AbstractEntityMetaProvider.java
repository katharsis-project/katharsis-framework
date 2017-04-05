package io.katharsis.jpa.meta.internal;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.jpa.meta.MetaEntityAttribute;
import io.katharsis.jpa.meta.MetaJpaDataObject;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.MetaPrimaryKey;
import io.katharsis.meta.provider.MetaProviderContext;

public abstract class AbstractEntityMetaProvider<T extends MetaJpaDataObject> extends AbstractJpaDataObjectProvider<T> {

	@Override
	public MetaElement createElement(Type type, MetaProviderContext context) {
		Class<?> rawClazz = ClassUtils.getRawType(type);
		Class<?> jpaSuperclass = getJpaSuperclass(rawClazz);
		MetaElement superMeta = null;
		if (jpaSuperclass != null) {
			superMeta = context.getLookup().getMeta(jpaSuperclass, MetaJpaDataObject.class);
		}
		T meta = newDataObject();
		meta.setElementType(meta);
		meta.setName(rawClazz.getSimpleName());
		meta.setImplementationType(type);
		meta.setSuperType((MetaDataObject) superMeta);
		if (superMeta != null) {
			((MetaDataObject) superMeta).addSubType(meta);
		}
		createAttributes(meta);
		setKey(meta);
		return meta;
	}
	
	private Class<?> getJpaSuperclass(Class<?> resourceClass) {
		Class<?> superclass = resourceClass.getSuperclass();
		while(superclass != Object.class){
			if(superclass.getAnnotation(Entity.class) != null || superclass.getAnnotation(MappedSuperclass.class) != null){
				return superclass;
			}
			superclass = superclass.getSuperclass();
		}
		return null;
	}

	private void setKey(T meta) {
		if (meta.getPrimaryKey() == null) {
			boolean generated = false;
			ArrayList<MetaAttribute> pkElements = new ArrayList<>();
			for (MetaAttribute attr : meta.getAttributes()) {
				if (attr.getAnnotation(Id.class) != null || attr.getAnnotation(EmbeddedId.class) != null) {
					pkElements.add(attr);

					boolean attrGenerated = attr.getAnnotation(GeneratedValue.class) != null;
					if (pkElements.size() == 1) {
						generated = attrGenerated;
					} else if (generated != attrGenerated) {
						throw new IllegalStateException(
								"cannot mix generated and not-generated primary key elements for " + meta.getId());
					}
				}
			}
			if (!pkElements.isEmpty()) {
				MetaPrimaryKey primaryKey = new MetaPrimaryKey();
				primaryKey.setName(meta.getName() + "$primaryKey");
				primaryKey.setElements(pkElements);
				primaryKey.setUnique(true);
				primaryKey.setParent(meta, true);
				primaryKey.setGenerated(generated);
				meta.setPrimaryKey(primaryKey);
			}
		}
	}

	protected abstract T newDataObject();

	@Override
	protected MetaAttribute createAttribute(T metaDataObject, PropertyDescriptor desc) {
		MetaEntityAttribute attr = new MetaEntityAttribute();
		attr.setName(desc.getName());
		attr.setParent(metaDataObject, true);
		if (hasJpaAnnotations(attr)) {
			ManyToMany manyManyAnnotation = attr.getAnnotation(ManyToMany.class);
			ManyToOne manyOneAnnotation = attr.getAnnotation(ManyToOne.class);
			OneToMany oneManyAnnotation = attr.getAnnotation(OneToMany.class);
			OneToOne oneOneAnnotation = attr.getAnnotation(OneToOne.class);
			Version versionAnnotation = attr.getAnnotation(Version.class);
			ElementCollection elemCollectionAnnotation = attr.getAnnotation(ElementCollection.class);

			attr.setVersion(versionAnnotation != null);

			FetchType fetchType = null;
			if (manyManyAnnotation != null) {
				fetchType = manyManyAnnotation.fetch();
			}
			if (oneManyAnnotation != null) {
				fetchType = oneManyAnnotation.fetch();
			}
			if (oneOneAnnotation != null) {
				fetchType = oneOneAnnotation.fetch();
			}

			attr.setAssociation(manyManyAnnotation != null || manyOneAnnotation != null || oneManyAnnotation != null
					|| oneOneAnnotation != null);

			boolean lazyCollection = elemCollectionAnnotation != null
					&& elemCollectionAnnotation.fetch() != FetchType.EAGER;
			boolean lazyAssociation = attr.isAssociation() && (fetchType == null || fetchType == FetchType.LAZY);

			attr.setLazy(lazyCollection || lazyAssociation);
		} else {
			attr.setDerived(true);
		}

		attr.setSortable(true);
		attr.setFilterable(true);

		return attr;
	}

	@Override
	public void onInitialized(MetaProviderContext context, MetaElement element) {
		super.onInitialized(context, element);
		if (element.getParent() instanceof MetaJpaDataObject && element instanceof MetaAttribute
				&& ((MetaAttribute) element).getOppositeAttribute() == null) {
			MetaAttribute attr = (MetaAttribute) element;
			String mappedBy = getMappedBy(attr);
			if (mappedBy != null) {

				MetaDataObject oppositeType = attr.getType().getElementType().asDataObject();
				MetaAttribute oppositeAttr = oppositeType.getAttribute(mappedBy);
				if (oppositeAttr == null) {
					throw new IllegalStateException(mappedBy + " opposite attribute not found for " + attr);
				}
				attr.setOppositeAttribute(oppositeAttr);
			}
		}
	}

	private String getMappedBy(MetaAttribute attr) {
		ManyToMany manyManyAnnotation = attr.getAnnotation(ManyToMany.class);
		OneToMany oneManyAnnotation = attr.getAnnotation(OneToMany.class);
		OneToOne oneOneAnnotation = attr.getAnnotation(OneToOne.class);
		String mappedBy = null;
		if (manyManyAnnotation != null) {
			mappedBy = manyManyAnnotation.mappedBy();
		}
		if (oneManyAnnotation != null) {
			mappedBy = oneManyAnnotation.mappedBy();
		}
		if (oneOneAnnotation != null) {
			mappedBy = oneOneAnnotation.mappedBy();
		}

		if (mappedBy != null && mappedBy.length() == 0) {
			mappedBy = null;
		}
		return mappedBy;
	}

	private boolean hasJpaAnnotations(MetaAttribute attribute) {
		List<Class<? extends Annotation>> annotationClasses = Arrays.asList(Id.class, EmbeddedId.class, Column.class,
				ManyToMany.class, ManyToOne.class, OneToMany.class, OneToOne.class, Version.class,
				ElementCollection.class);
		for (Class<? extends Annotation> annotationClass : annotationClasses) {
			if (attribute.getAnnotation(annotationClass) != null) {
				return true;
			}
		}
		return false;
	}
}
