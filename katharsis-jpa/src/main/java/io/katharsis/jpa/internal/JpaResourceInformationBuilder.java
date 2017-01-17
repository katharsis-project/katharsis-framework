package io.katharsis.jpa.internal;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OptimisticLockException;

import com.fasterxml.jackson.databind.JsonNode;

import io.katharsis.jpa.annotations.JpaMergeRelations;
import io.katharsis.jpa.annotations.JpaResource;
import io.katharsis.jpa.meta.MetaEntity;
import io.katharsis.jpa.meta.MetaJpaDataObject;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.MetaKey;
import io.katharsis.meta.model.resource.MetaJsonObject;
import io.katharsis.resource.Document;
import io.katharsis.resource.Resource;
import io.katharsis.resource.annotations.JsonApiLinksInformation;
import io.katharsis.resource.annotations.JsonApiMetaInformation;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.field.ResourceField.LookupIncludeBehavior;
import io.katharsis.resource.field.ResourceField.ResourceFieldType;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder.AnnotatedResourceField;
import io.katharsis.resource.information.DefaultResourceInstanceBuilder;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInstanceBuilder;
import io.katharsis.utils.StringUtils;

/**
 * Extracts resource information from JPA and Katharsis annotations. Katharsis
 * annotations take precedence.
 */
public class JpaResourceInformationBuilder implements ResourceInformationBuilder {

	private static final String ENTITY_NAME_SUFFIX = "Entity";

	private MetaLookup jpaMetaLookup;

	public JpaResourceInformationBuilder(MetaLookup jpaMetaLookup) {
		this.jpaMetaLookup = jpaMetaLookup;
	}

	@Override
	public boolean accept(Class<?> resourceClass) {
		// needs to be configured for being exposed
		if (resourceClass.getAnnotation(JpaResource.class) != null) {
			return true;
		}

		// needs to be an entity
		MetaElement meta = jpaMetaLookup.getMeta(resourceClass, MetaJpaDataObject.class, true);
		if (meta instanceof MetaEntity) {
			MetaEntity metaEntity = (MetaEntity) meta;
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

		MetaDataObject meta;
		DefaultResourceInstanceBuilder instanceBuilder;
		if (resourceClass.getAnnotation(JpaResource.class) != null) {
			// non-entities (like dtos subclassing entities) use default
			// instantiation
			meta = jpaMetaLookup.getMeta(resourceClass, MetaJsonObject.class);
			instanceBuilder = new DefaultResourceInstanceBuilder(resourceClass);
		} else {
			meta = jpaMetaLookup.getMeta(resourceClass, MetaEntity.class).asDataObject();
			instanceBuilder = new JpaResourceInstanceBuilder((MetaEntity) meta, resourceClass);
		}

		List<ResourceField> fields = getFields(meta);
		Set<String> ignoredFields = getIgnoredFields(meta);

		return new JpaResourceInformation(meta, resourceClass, resourceType, instanceBuilder, fields, ignoredFields);
	}

	class JpaResourceInstanceBuilder<T> extends DefaultResourceInstanceBuilder<T> {

		private MetaEntity meta;

		public JpaResourceInstanceBuilder(MetaEntity meta, Class<T> resourceClass) {
			super(resourceClass);
			this.meta = meta;
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

		private MetaDataObject meta;

		private Set<String> ignoredFields;

		public JpaResourceInformation(MetaDataObject meta, Class<?> resourceClass, String resourceType, // NOSONAR
				ResourceInstanceBuilder<?> instanceBuilder, List<ResourceField> fields, Set<String> ignoredFields) {
			super(resourceClass, resourceType, instanceBuilder, fields);
			this.meta = meta;
			this.ignoredFields = ignoredFields;
		}

		@Override
		@Deprecated // Temporary method until proper
					// versioning/locking/timestamping is implemented
		public void verify(Object entity, Document requestDocument) {
			checkOptimisticLocking(entity, requestDocument.getSingleData().get());
		}

		private void checkOptimisticLocking(Object entity, Resource resource) {
			MetaAttribute versionAttr = meta.getVersionAttribute();
			if (versionAttr != null) {
				JsonNode versionNode = resource.getAttributes().get(versionAttr.getName());
				if (versionNode != null) {
					Object requestVersion = versionAttr.getType().fromString(versionNode.asText());
					Object currentVersion = versionAttr.getValue(entity);
					if (!currentVersion.equals(requestVersion))
						throw new OptimisticLockException(resource.getId() + " changed from version " + requestVersion + " to " + currentVersion);
				}
			}
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
		JpaResource annotation = entityClass.getAnnotation(JpaResource.class);
		if (annotation != null) {
			return annotation.type();
		}

		String name = entityClass.getSimpleName();
		if (name.endsWith(ENTITY_NAME_SUFFIX)) {
			name = name.substring(0, name.length() - ENTITY_NAME_SUFFIX.length());
		}
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	protected List<ResourceField> getFields(MetaDataObject meta) {
		List<ResourceField> fields = new ArrayList<>();

		for (MetaAttribute attr : meta.getAttributes()) {
			if (!isIgnored(attr)) {
				fields.add(toField(meta, attr));
			}
		}

		return fields;
	}

	private boolean isAssociation(MetaDataObject meta, MetaAttribute attr) {
		// merged attribute are handled as normal data attributes
		JpaMergeRelations mergeAnnotation = meta.getImplementationClass().getAnnotation(JpaMergeRelations.class);
		if (mergeAnnotation != null) {
			String[] mergedAttrs = mergeAnnotation.attributes();
			for (String mergedAttr : mergedAttrs) {
				if (mergedAttr.equals(attr.getName())) {
					return false;
				}
			}
		}

		return attr.isAssociation();
	}

	protected Set<String> getIgnoredFields(MetaDataObject meta) {
		Set<String> fields = new HashSet<>();
		for (MetaAttribute attr : meta.getAttributes()) {
			if (isIgnored(attr)) {
				fields.add(attr.getName());
			}
		}
		return fields;
	}

	protected boolean isIgnored(MetaAttribute attr) {
		return false;
	}

	protected ResourceField toField(MetaDataObject meta, MetaAttribute attr) {
		String jsonName = attr.getName();
		String underlyingName = attr.getName();
		Class<?> type = attr.getType().getImplementationClass();
		Type genericType = attr.getType().getImplementationType();

		Collection<Annotation> annotations = attr.getAnnotations();

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

		MetaKey primaryKey = meta.getPrimaryKey();
		boolean id = primaryKey.getElements().contains(attr);
		boolean linksInfo = attr.getAnnotation(JsonApiLinksInformation.class) != null;
		boolean metaInfo = attr.getAnnotation(JsonApiMetaInformation.class) != null;
		boolean association = isAssociation(meta, attr);
		ResourceFieldType resourceFieldType = ResourceFieldType.get(id, linksInfo, metaInfo, association);

		// related repositories should lookup, we ignore the hibernate proxies
		LookupIncludeBehavior lookupIncludeBehavior = AnnotatedResourceField.getLookupIncludeBehavior(annotations, LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
		return new ResourceField(jsonName, underlyingName, resourceFieldType, type, genericType, oppositeName, lazy, includeByDefault, lookupIncludeBehavior);
	}
}
