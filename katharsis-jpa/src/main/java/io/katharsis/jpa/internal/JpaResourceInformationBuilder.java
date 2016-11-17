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
import javax.persistence.OptimisticLockException;

import com.fasterxml.jackson.databind.JsonNode;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaElement;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.request.dto.DataBody;
import io.katharsis.resource.field.ResourceAttributesBridge;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceField.LookupIncludeBehavior;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder.AnnotatedResourceField;
import io.katharsis.utils.StringUtils;
import io.katharsis.resource.information.DefaultResourceInstanceBuilder;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInstanceBuilder;

/**
 * Extracts resource information from JPA and Katharsis annotations. Katharsis
 * annotations take precedence.
 */
public class JpaResourceInformationBuilder implements ResourceInformationBuilder {

	private static final String ENTITY_NAME_SUFFIX = "Entity";

	private EntityManager em;
	private Set<Class<?>> exposedResourceClasses;
	private MetaLookup metaLookup;

	public JpaResourceInformationBuilder(MetaLookup metaLookup, EntityManager em,
			Set<Class<? extends Object>> exposedResourceClasses) {
		this.em = em;
		this.exposedResourceClasses = exposedResourceClasses;
		this.metaLookup = metaLookup;
	}

	@Override
	public boolean accept(Class<?> resourceClass) {
		// needs to be configured for being exposed 
		if (!exposedResourceClasses.contains(resourceClass)) {
			return false;
		}

		// needs to be an entity
		MetaElement meta = metaLookup.getMeta(resourceClass);
		if (meta instanceof MetaEntity) {
			MetaEntity metaEntity = meta.asEntity();
			MetaKey primaryKey = metaEntity.getPrimaryKey();
			return primaryKey != null && primaryKey.getElements().size() == 1;
		} else {
			// note that DTOs cannot be handled here
			return false;
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResourceInformation build(final Class<?> resourceClass) {
		String resourceType = getResourceType(resourceClass);

		MetaEntity meta = metaLookup.getMeta(resourceClass).asEntity();
		ResourceField idField = getIdField(meta);
		Set<ResourceField> attributeFields = getAttributeFields(meta, false);
		Set<ResourceField> relationshipFields = getAttributeFields(meta, true);
		Set<String> ignoredFields = getIgnoredFields(meta);

		// make sure that existing managed object are used where available
		JpaResourceInstanceBuilder instanceBuilder = new JpaResourceInstanceBuilder(resourceClass);

		return new JpaResourceInformation(meta, resourceClass, resourceType, instanceBuilder, idField,
				new ResourceAttributesBridge(attributeFields, resourceClass), relationshipFields, ignoredFields);
	}

	class JpaResourceInstanceBuilder<T> extends DefaultResourceInstanceBuilder<T> {
		private MetaEntity meta;

		public JpaResourceInstanceBuilder(Class<T> resourceClass) {
			super(resourceClass);
			meta = metaLookup.getMeta(resourceClass).asEntity();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T buildResource(DataBody body) {
			String strId = body.getId();

			// use managed entities on the server-side
			if (strId != null && em != null) {
				Object id = meta.getPrimaryKey().fromKeyString(strId);
				Object entity = em.find(meta.getImplementationClass(), id);
				if (entity != null) {
					// version check
					checkOptimisticLocking(entity, body);
					return (T) entity;
				}
			}
			return super.buildResource(body);
		}

		private void checkOptimisticLocking(Object entity, DataBody body) {
			MetaAttribute versionAttr = meta.getVersionAttribute();
			if (versionAttr != null) {
				JsonNode versionNode = body.getAttributes().get(versionAttr.getName());
				if (versionNode != null) {
					Object requestVersion = versionAttr.getType().fromString(versionNode.asText());
					Object currentVersion = versionAttr.getValue(entity);
					if (!currentVersion.equals(requestVersion))
						throw new OptimisticLockException(
								body.getId() + " changed from version " + requestVersion + " to " + currentVersion);
				}
			}
		}

		@Override
		public int hashCode() {
			return super.hashCode() | meta.getName().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return super.equals(obj) && obj instanceof JpaResourceInstanceBuilder;
		}
	}

	class JpaResourceInformation extends ResourceInformation {

		private MetaEntity meta;
		private Set<String> ignoredFields;

		public JpaResourceInformation(MetaEntity meta, Class<?> resourceClass, String resourceType,
				ResourceField idField, ResourceAttributesBridge<?> attributeFields,
				Set<ResourceField> relationshipFields, Set<String> ignoredFields) {
			this(meta, resourceClass, resourceType, null, idField, attributeFields, relationshipFields, ignoredFields,
					null, null);
		}

		public JpaResourceInformation(MetaEntity meta, Class<?> resourceClass, String resourceType, // NOSONAR
				ResourceInstanceBuilder<?> instanceBuilder, ResourceField idField,
				ResourceAttributesBridge<?> attributeFields, Set<ResourceField> relationshipFields,
				Set<String> ignoredFields) {
			this(meta, resourceClass, resourceType, instanceBuilder, idField, attributeFields, relationshipFields,
					ignoredFields, null, null);
		}

		public JpaResourceInformation(MetaEntity meta, Class<?> resourceClass, String resourceType, // NOSONAR
				ResourceInstanceBuilder<?> instanceBuilder, ResourceField idField,
				ResourceAttributesBridge<?> attributeFields, Set<ResourceField> relationshipFields,
				Set<String> ignoredFields, String metaFieldName, String linksFieldName) {
			super(resourceClass, resourceType, instanceBuilder, idField, attributeFields, relationshipFields,
					metaFieldName, linksFieldName);
			this.meta = meta;
			this.ignoredFields = ignoredFields;
		}

		/**
		 * Specialized ID handling to take care of embeddables and compound
		 * primary keys.
		 */
		@Override
		public Serializable parseIdString(String id) {
			return (Serializable) meta.getPrimaryKey().fromKeyString(id);
		}

		/**
		 * Specialized ID handling to take care of embeddables and compound
		 * primary keys.
		 */
		@Override
		public String toIdString(Object id) {
			return meta.getPrimaryKey().toKeyString(id);
		}

		@Override
		public Set<String> getNotAttributeFields() {
			Set<String> notAttributeFields = super.getNotAttributeFields();
			notAttributeFields.addAll(ignoredFields);
			return notAttributeFields;
		}

		@Override
		public int hashCode() {
			return super.hashCode() | ignoredFields.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return super.equals(obj) && obj instanceof JpaResourceInformation;
		}
	}

	protected String getResourceType(Class<?> entityClass) {
		String name = entityClass.getSimpleName();
		if (name.endsWith(ENTITY_NAME_SUFFIX)) {
			name = name.substring(0, name.length() - ENTITY_NAME_SUFFIX.length());
		}
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	protected Set<ResourceField> getAttributeFields(MetaEntity meta, boolean relations) {
		Set<ResourceField> fields = new HashSet<>();

		MetaAttribute primaryKeyAttr = JpaRepositoryUtils.getPrimaryKeyAttr(meta);
		for (MetaAttribute attr : meta.getAttributes()) {
			if (attr.equals(primaryKeyAttr) || attr.isAssociation() != relations || isIgnored(attr))
				continue;

			fields.add(toField(attr));
		}

		return fields;
	}

	protected Set<String> getIgnoredFields(MetaEntity meta) {
		Set<String> fields = new HashSet<>();
		for (MetaAttribute attr : meta.getAttributes()) {
			if (isIgnored(attr)) {
				fields.add(attr.getName());
			}
		}
		return fields;
	}

	protected boolean isIgnored(MetaAttribute attr) {
		MetaType type = attr.getType();
		if (type.isCollection()) {
			type = type.asCollection().getElementType();
		}
		return attr.isAssociation() && !exposedResourceClasses.contains(type.getImplementationClass());
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
		String oppositeName = null;
		boolean lazyDefault = false;
		for (Annotation annotation : annotations) {
			if (annotation instanceof ElementCollection) {
				lazyDefault = ((ElementCollection) annotation).fetch() == FetchType.LAZY;
			} else if (annotation instanceof ManyToOne) {
				lazyDefault = ((ManyToOne) annotation).fetch() == FetchType.LAZY;
			} else if (annotation instanceof OneToMany) {
				lazyDefault = ((OneToMany) annotation).fetch() == FetchType.LAZY;
				oppositeName = StringUtils.emptyToNull(((OneToMany) annotation).mappedBy());
			} else if (annotation instanceof ManyToMany) {
				lazyDefault = ((ManyToMany) annotation).fetch() == FetchType.LAZY;
				oppositeName = StringUtils.emptyToNull(((ManyToMany) annotation).mappedBy());
			}
		}

		// read Katharsis annotations
		boolean lazy = AnnotatedResourceField.isLazy(annotations, lazyDefault);
		boolean includeByDefault = AnnotatedResourceField.getIncludeByDefault(annotations);
		
		// related repositories should lookup, we ignore the hibernate proxies
		LookupIncludeBehavior lookupIncludeBehavior = AnnotatedResourceField.getLookupIncludeBehavior(annotations, LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
		return new ResourceField(jsonName, underlyingName, type, genericType, oppositeName, lazy, includeByDefault,
				lookupIncludeBehavior);
	}

	protected ResourceField getIdField(MetaEntity meta) {
		MetaAttribute attr = JpaRepositoryUtils.getPrimaryKeyAttr(meta);
		return toField(attr);
	}

}
