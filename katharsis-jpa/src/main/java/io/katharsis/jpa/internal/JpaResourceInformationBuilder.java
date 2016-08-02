package io.katharsis.jpa.internal;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaElement;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.request.dto.DataBody;
import io.katharsis.resource.field.ResourceAttributesBridge;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceField.LookupIncludeBehavior;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder.AnnotatedResourceField;
import io.katharsis.resource.information.DefaultResourceInstanceBuilder;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInstanceBuilder;
import io.katharsis.utils.parser.TypeParser;

/**
 * Extracts resource information from JPA and Katharsis annotations. Katharsis
 * annotations take precedence.
 */
public class JpaResourceInformationBuilder implements ResourceInformationBuilder {

	private static final String ENTITY_NAME_SUFFIX = "Entity";

	private TypeParser typeParser = new TypeParser();

	private EntityManager em;

	public JpaResourceInformationBuilder(EntityManager em) {
		this.em = em;
	}

	@Override
	public boolean accept(Class<?> resourceClass) {
		MetaElement meta = MetaLookup.INSTANCE.getMeta(resourceClass);
		if (meta instanceof MetaEntity) {
			MetaEntity metaEntity = meta.asEntity();
			MetaKey primaryKey = metaEntity.getPrimaryKey();
			return primaryKey != null && primaryKey.getElements().size() == 1;
		} else {
			return false;
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResourceInformation build(final Class<?> resourceClass) {
		final MetaEntity meta = MetaLookup.INSTANCE.getMeta(resourceClass).asEntity();

		String resourceType = getResourceType(resourceClass);

		ResourceField idField = getIdField(meta);
		Set<ResourceField> attributeFields = getAttributeFields(meta, false);
		Set<ResourceField> relationshipFields = getAttributeFields(meta, true);

		// make sure that existing managed object are used where available
		ResourceInstanceBuilder<?> instanceBuilder = new DefaultResourceInstanceBuilder<Object>((Class) resourceClass) {
			@Override
			public Object buildResource(DataBody body) {
				String strId = body.getId();

				// use managed entities on the server-side
				if (strId != null && em != null) {
					MetaAttribute primaryKeyAttr = meta.getPrimaryKey().getUniqueElement();
					Class<?> implClass = primaryKeyAttr.getType().getImplementationClass();
					Serializable id = typeParser.parse(strId, (Class) implClass);
					Object entity = em.find(resourceClass, id);
					if (entity != null) {
						return entity;
					}
				}
				return super.buildResource(body);
			}
		};

		return new ResourceInformation(resourceClass, resourceType, instanceBuilder, idField,
				new ResourceAttributesBridge(attributeFields, resourceClass), relationshipFields);
	}

	protected String getResourceType(Class<?> entityClass) {
		String name = entityClass.getSimpleName();
		if (name.endsWith(ENTITY_NAME_SUFFIX)) {
			name = name.substring(0, name.length() - ENTITY_NAME_SUFFIX.length());
		}
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	protected Set<ResourceField> getAttributeFields(MetaEntity meta, boolean relations) {
		Set<ResourceField> fields = new HashSet<ResourceField>();

		MetaAttribute primaryKeyAttr = JpaRepositoryUtils.getPrimaryKeyAttr(meta);
		for (MetaAttribute attr : meta.getAttributes()) {
			if (attr.equals(primaryKeyAttr))
				continue;
			if (attr.isAssociation() != relations)
				continue;
			fields.add(toField(attr));
		}

		return fields;
	}

	protected ResourceField toField(MetaAttribute attr) {
		String jsonName = attr.getName();
		String underlyingName = attr.getName();
		Class<?> type = attr.getType().getImplementationClass();
		Type genericType = attr.getType().getImplementationType();

		Class<?> beanClass = attr.getParent().getImplementationClass();
		Field declaredField;
		try {
			declaredField = beanClass.getDeclaredField(attr.getName());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		List<Annotation> annotations = Arrays.asList(declaredField.getAnnotations());

		// use JPA annotations as default
		boolean lazyDefault = false;
		for (Annotation annotation : annotations) {
			if (annotation instanceof ElementCollection) {
				lazyDefault = ((ElementCollection) annotation).fetch() == FetchType.LAZY;
			} else if (annotation instanceof ManyToOne) {
				lazyDefault = ((ManyToOne) annotation).fetch() == FetchType.LAZY;
			} else if (annotation instanceof OneToMany) {
				lazyDefault = ((OneToMany) annotation).fetch() == FetchType.LAZY;
			} else if (annotation instanceof ManyToMany) {
				lazyDefault = ((ManyToMany) annotation).fetch() == FetchType.LAZY;
			}
		}

		// read Katharsis annotations
		boolean lazy = AnnotatedResourceField.isLazy(annotations, lazyDefault);
		boolean includeByDefault = AnnotatedResourceField.getIncludeByDefault(annotations);
		LookupIncludeBehavior lookupIncludeBehavior = AnnotatedResourceField.getLookupIncludeBehavior(annotations);
		return new ResourceField(jsonName, underlyingName, type, genericType, lazy, includeByDefault,
				lookupIncludeBehavior);
	}

	protected ResourceField getIdField(MetaEntity meta) {
		MetaAttribute attr = JpaRepositoryUtils.getPrimaryKeyAttr(meta);
		return toField(attr);
	}

}
