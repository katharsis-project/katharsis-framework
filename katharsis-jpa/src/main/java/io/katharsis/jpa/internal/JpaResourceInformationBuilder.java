package io.katharsis.jpa.internal;

import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OptimisticLockException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder.AnnotatedResourceField;
import io.katharsis.core.internal.resource.DefaultResourceInstanceBuilder;
import io.katharsis.core.internal.resource.ResourceFieldImpl;
import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.core.internal.utils.StringUtils;
import io.katharsis.jpa.annotations.JpaMergeRelations;
import io.katharsis.jpa.annotations.JpaResource;
import io.katharsis.jpa.meta.MetaEntity;
import io.katharsis.jpa.meta.MetaJpaDataObject;
import io.katharsis.meta.MetaLookup;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.MetaKey;
import io.katharsis.meta.model.MetaType;
import io.katharsis.meta.model.resource.MetaJsonObject;
import io.katharsis.resource.Document;
import io.katharsis.resource.Resource;
import io.katharsis.resource.annotations.JsonApiLinksInformation;
import io.katharsis.resource.annotations.JsonApiMetaInformation;
import io.katharsis.resource.annotations.LookupIncludeBehavior;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceFieldType;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilderContext;
import io.katharsis.resource.information.ResourceInstanceBuilder;
import io.katharsis.utils.parser.TypeParser;

/**
 * Extracts resource information from JPA and Katharsis annotations. Katharsis
 * annotations take precedence.
 */
public class JpaResourceInformationBuilder implements ResourceInformationBuilder {

	private static final String ENTITY_NAME_SUFFIX = "Entity";

	private MetaLookup jpaMetaLookup;

	private ResourceInformationBuilderContext context;

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

		TypeParser typeParser = context.getTypeParser();
		return new JpaResourceInformation(typeParser, meta, resourceClass, resourceType, instanceBuilder, fields,
				ignoredFields);
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

		public JpaResourceInformation(TypeParser typeParser, MetaDataObject meta, Class<?> resourceClass,
				String resourceType, // NOSONAR
				ResourceInstanceBuilder<?> instanceBuilder, List<ResourceField> fields, Set<String> ignoredFields) {
			super(typeParser, resourceClass, resourceType, instanceBuilder, fields);
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
					Object requestVersion = context.getTypeParser().parse(versionNode.asText(),
							(Class) versionAttr.getType().getImplementationClass());
					Object currentVersion = versionAttr.getValue(entity);
					if (!currentVersion.equals(requestVersion))
						throw new OptimisticLockException(
								resource.getId() + " changed from version " + requestVersion + " to " + currentVersion);
				}
			}
		}

		/**
		 * Specialized ID handling to take care of embeddables and compound
		 * primary keys.
		 */
		@Override
		public Serializable parseIdString(String id) {
			return fromKeyString(id);
		}

		private Serializable fromKeyString(String id) {

			MetaKey primaryKey = meta.getPrimaryKey();
			MetaAttribute attr = primaryKey.getUniqueElement();
			return (Serializable) fromKeyString(attr.getType(), id);
		}

		private Object fromKeyString(MetaType type, String idString) {
			// => support compound keys with unique ids
			if (type instanceof MetaDataObject) {
				return parseEmbeddableString((MetaDataObject) type, idString);
			} else {
				return context.getTypeParser().parse(idString, (Class) type.getImplementationClass());
			}
		}

		private Object parseEmbeddableString(MetaDataObject embType, String idString) {
			String[] keyElements = idString.split(MetaKey.ID_ELEMENT_SEPARATOR);

			Object id = ClassUtils.newInstance(embType.getImplementationClass());

			List<? extends MetaAttribute> embAttrs = embType.getAttributes();
			if (keyElements.length != embAttrs.size()) {
				throw new UnsupportedOperationException("failed to parse " + idString + " for " + embType.getId());
			}
			for (int i = 0; i < keyElements.length; i++) {
				MetaAttribute embAttr = embAttrs.get(i);
				Object idElement = fromKeyString(embAttr.getType(), keyElements[i]);
				embAttr.setValue(id, idElement);
			}
			return id;
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

	@Override
	public String getResourceType(Class<?> entityClass) {
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
		String oppositeResourceType = association
				? AnnotationResourceInformationBuilder.getResourceType(genericType, context) : null;

		// related repositories should lookup, we ignore the hibernate proxies
		LookupIncludeBehavior lookupIncludeBehavior = AnnotatedResourceField.getLookupIncludeBehavior(annotations,
				LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
		return new ResourceFieldImpl(jsonName, underlyingName, resourceFieldType, type, genericType,
				oppositeResourceType, oppositeName, lazy, includeByDefault, lookupIncludeBehavior);
	}

	@Override
	public void init(ResourceInformationBuilderContext context) {
		this.context = context;
	}
}
