package io.katharsis.core.internal.resource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.core.internal.utils.FieldOrderedComparator;
import io.katharsis.core.internal.utils.StringUtils;
import io.katharsis.errorhandling.exception.RepositoryAnnotationNotFoundException;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiIncludeByDefault;
import io.katharsis.resource.annotations.JsonApiLinksInformation;
import io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.katharsis.resource.annotations.JsonApiMetaInformation;
import io.katharsis.resource.annotations.JsonApiRelation;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.annotations.LookupIncludeBehavior;
import io.katharsis.resource.annotations.SerializeType;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceFieldType;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilderContext;
import io.katharsis.utils.Optional;

/**
 * A builder which creates ResourceInformation instances of a specific class. It
 * extracts information about a resource from annotations and information about
 * fields and getters.
 */
public class AnnotationResourceInformationBuilder implements ResourceInformationBuilder {

	private final ResourceFieldNameTransformer resourceFieldNameTransformer;
	private ResourceInformationBuilderContext context;

	public AnnotationResourceInformationBuilder(ResourceFieldNameTransformer resourceFieldNameTransformer) {
		this.resourceFieldNameTransformer = resourceFieldNameTransformer;
	}

	@Override
	public boolean accept(Class<?> resourceClass) {
		return resourceClass.getAnnotation(JsonApiResource.class) != null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public ResourceInformation build(Class<?> resourceClass) {
		List<AnnotatedResourceField> resourceFields = getResourceFields(resourceClass);

		String resourceType = getResourceType(resourceClass);

		Optional<JsonPropertyOrder> propertyOrder = ClassUtils.getAnnotation(resourceClass, JsonPropertyOrder.class);
		if (propertyOrder.isPresent()) {
			JsonPropertyOrder propertyOrderAnnotation = propertyOrder.get();
			Collections.sort(resourceFields, new FieldOrderedComparator(propertyOrderAnnotation.value(), propertyOrderAnnotation.alphabetic()));
		}

		DefaultResourceInstanceBuilder<?> instanceBuilder = new DefaultResourceInstanceBuilder(resourceClass);

		return new ResourceInformation(context.getTypeParser(), resourceClass, resourceType, instanceBuilder, (List) resourceFields);
	}

	@Override
	public String getResourceType(Class<?> resourceClass) {
		Annotation[] annotations = resourceClass.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof JsonApiResource) {
				JsonApiResource apiResource = (JsonApiResource) annotation;
				return apiResource.type();
			}
		}
		// won't reach this
		throw new RepositoryAnnotationNotFoundException(resourceClass.getName());
	}

	private List<AnnotatedResourceField> getResourceFields(Class<?> resourceClass) {
		List<Field> classFields = ClassUtils.getClassFields(resourceClass);
		List<Method> classGetters = ClassUtils.getClassGetters(resourceClass);

		List<ResourceFieldWrapper> resourceClassFields = getFieldResourceFields(classFields);
		List<ResourceFieldWrapper> resourceGetterFields = getGetterResourceFields(classGetters);
		return getResourceFields(resourceClassFields, resourceGetterFields);
	}

	private List<ResourceFieldWrapper> getFieldResourceFields(List<Field> classFields) {
		List<ResourceFieldWrapper> fieldWrappers = new ArrayList<>(classFields.size());
		for (Field field : classFields) {
			String jsonName = resourceFieldNameTransformer.getName(field);
			String underlyingName = field.getName();
			List<Annotation> annotations = Arrays.asList(field.getAnnotations());
			ResourceFieldType resourceFieldType = AnnotatedResourceField.getResourceFieldType(annotations);
			String oppositeResourceType = resourceFieldType == ResourceFieldType.RELATIONSHIP ? getResourceType(field.getGenericType(), context) : null;
			AnnotatedResourceField resourceField = new AnnotatedResourceField(jsonName, underlyingName, field.getType(), field.getGenericType(), oppositeResourceType, annotations);
			if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
				fieldWrappers.add(new ResourceFieldWrapper(resourceField, true));
			} else {
				fieldWrappers.add(new ResourceFieldWrapper(resourceField, false));
			}
		}
		return fieldWrappers;
	}

	public static String getResourceType(Type genericType, ResourceInformationBuilderContext context) {
		Type elementType = genericType;
		if (Iterable.class.isAssignableFrom(ClassUtils.getRawType(genericType))) {
			elementType = ClassUtils.getRawType(((ParameterizedType) genericType).getActualTypeArguments()[0]);
		}
		Class<?> rawType = ClassUtils.getRawType(elementType);
		return context.accept(rawType) ? context.getResourceType(rawType) : null;
	}

	private List<ResourceFieldWrapper> getGetterResourceFields(List<Method> classGetters) {
		List<ResourceFieldWrapper> fieldWrappers = new ArrayList<>(classGetters.size());
		for (Method getter : classGetters) {
			String jsonName = resourceFieldNameTransformer.getName(getter);
			String underlyingName = resourceFieldNameTransformer.getMethodName(getter);
			List<Annotation> annotations = Arrays.asList(getter.getAnnotations());
			ResourceFieldType resourceFieldType = AnnotatedResourceField.getResourceFieldType(annotations);
			String oppositeResourceType = resourceFieldType == ResourceFieldType.RELATIONSHIP ? getResourceType(getter.getGenericReturnType(), context) : null;
			AnnotatedResourceField resourceField = new AnnotatedResourceField(jsonName, underlyingName, getter.getReturnType(), getter.getGenericReturnType(), oppositeResourceType, annotations);
			if (Modifier.isStatic(getter.getModifiers())) {
				fieldWrappers.add(new ResourceFieldWrapper(resourceField, true));
			} else {
				fieldWrappers.add(new ResourceFieldWrapper(resourceField, false));
			}
		}
		return fieldWrappers;
	}

	private List<AnnotatedResourceField> getResourceFields(List<ResourceFieldWrapper> resourceClassFields, List<ResourceFieldWrapper> resourceGetterFields) {
		Map<String, AnnotatedResourceField> resourceFieldMap = new HashMap<>();

		for (ResourceFieldWrapper fieldWrapper : resourceClassFields) {
			if (!fieldWrapper.isDiscarded())
				resourceFieldMap.put(fieldWrapper.getResourceField().getUnderlyingName(), fieldWrapper.getResourceField());
		}

		for (ResourceFieldWrapper fieldWrapper : resourceGetterFields) {
			if (!fieldWrapper.isDiscarded()) {
				String originalName = fieldWrapper.getResourceField().getUnderlyingName();
				AnnotatedResourceField field = fieldWrapper.getResourceField();
				if (resourceFieldMap.containsKey(originalName)) {
					resourceFieldMap.put(originalName, mergeAnnotations(resourceFieldMap.get(originalName), field, context));
				} else if (!hasDiscardedField(fieldWrapper, resourceClassFields)) {
					resourceFieldMap.put(originalName, field);
				}
			}
		}

		return discardIgnoredField(resourceFieldMap.values());
	}

	private List<AnnotatedResourceField> discardIgnoredField(Collection<AnnotatedResourceField> resourceFieldValues) {
		List<AnnotatedResourceField> resourceFields = new LinkedList<>();
		for (AnnotatedResourceField resourceField : resourceFieldValues) {
			if (!resourceField.isAnnotationPresent(JsonIgnore.class)) {
				resourceFields.add(resourceField);
			}
		}

		return resourceFields;
	}

	private static boolean hasDiscardedField(ResourceFieldWrapper fieldWrapper, List<ResourceFieldWrapper> resourceClassFields) {
		for (ResourceFieldWrapper resourceFieldWrapper : resourceClassFields) {
			if (fieldWrapper.getResourceField().getUnderlyingName().equals(resourceFieldWrapper.getResourceField().getUnderlyingName())) {
				return true;
			}
		}
		return false;
	}

	private static AnnotatedResourceField mergeAnnotations(AnnotatedResourceField fromField, AnnotatedResourceField fromMethod, ResourceInformationBuilderContext context) {
		List<Annotation> annotations = new ArrayList<>(fromField.getAnnotations());
		annotations.addAll(fromMethod.getAnnotations());

		Class<?> fieldType = mergeFieldType(fromField, fromMethod);
		Type fieldGenericType = mergeGenericType(fromField, fromMethod);
		String oppositeResourceType = getResourceType(fieldGenericType, context);
		return new AnnotatedResourceField(fromField.getJsonName(), fromField.getUnderlyingName(), fieldType, fieldGenericType, oppositeResourceType, annotations);
	}

	private static Class<?> mergeFieldType(AnnotatedResourceField fromField, AnnotatedResourceField fromMethod) {
		if (hasKatharsisAnnotation(fromField.getAnnotations())) {
			return fromField.getType();
		} else {
			return fromMethod.getType();
		}
	}

	private static Type mergeGenericType(AnnotatedResourceField fromField, AnnotatedResourceField fromMethod) {
		if (hasKatharsisAnnotation(fromField.getAnnotations())) {
			return fromField.getGenericType();
		} else {
			return fromMethod.getGenericType();
		}
	}

	private static boolean hasKatharsisAnnotation(List<Annotation> annotations) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType() == JsonApiId.class
					|| annotation.annotationType() == JsonApiRelation.class
					|| annotation.annotationType() == JsonApiToOne.class
					|| annotation.annotationType() == JsonApiToMany.class
					|| annotation.annotationType() == JsonApiMetaInformation.class || annotation.annotationType() == JsonApiLinksInformation.class) {
				return true;
			}
		}
		return false;
	}

	public static class ResourceFieldWrapper {
		private AnnotatedResourceField resourceField;
		private boolean discarded;

		public ResourceFieldWrapper(AnnotatedResourceField resourceField, boolean discarded) {
			this.resourceField = resourceField;
			this.discarded = discarded;
		}

		public AnnotatedResourceField getResourceField() {
			return resourceField;
		}

		public boolean isDiscarded() {
			return discarded;
		}
	}

	public static class AnnotatedResourceField extends ResourceFieldImpl {

		private List<Annotation> annotations;

		public AnnotatedResourceField(String jsonName, String underlyingName, Class<?> type, Type genericType, String oppositeResourceType, List<Annotation> annotations) {
			super(jsonName, underlyingName, getResourceFieldType(annotations), type, genericType, oppositeResourceType, getOppositeName(annotations), isLazy(annotations), getIncludeByDefault(annotations),
					getLookupIncludeBehavior(annotations));
			this.annotations = annotations;
		}

		private static String getOppositeName(List<Annotation> annotations) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonApiToMany) {
					return StringUtils.emptyToNull(((JsonApiToMany) annotation).opposite());
				}
				if (annotation instanceof JsonApiToOne) {
					return StringUtils.emptyToNull(((JsonApiToOne) annotation).opposite());
				}
				if (annotation instanceof JsonApiRelation) {
					return StringUtils.emptyToNull(((JsonApiRelation) annotation).opposite());
				}
			}
			return null;
		}

		public static boolean getIncludeByDefault(Collection<Annotation> annotations) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonApiRelation) {
					JsonApiRelation jsonApiRelation = (JsonApiRelation) annotation;
					return jsonApiRelation.serialize() == SerializeType.EAGER;
				}
				if (annotation instanceof JsonApiIncludeByDefault) {
					return true;
				}
			}
			return false;
		}

		public static LookupIncludeBehavior getLookupIncludeBehavior(Collection<Annotation> annotations) {
			return getLookupIncludeBehavior(annotations, LookupIncludeBehavior.NONE);
		}

		public static LookupIncludeBehavior getLookupIncludeBehavior(Collection<Annotation> annotations, LookupIncludeBehavior defaultBehavior) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonApiRelation) {
					JsonApiRelation jsonApiRelation = (JsonApiRelation) annotation;
					return jsonApiRelation.lookUp();
				}
				if (annotation instanceof JsonApiLookupIncludeAutomatically) {
					JsonApiLookupIncludeAutomatically includeAnnotation = (JsonApiLookupIncludeAutomatically) annotation;
					if (includeAnnotation.overwrite())
						return LookupIncludeBehavior.AUTOMATICALLY_ALWAYS;
					else
						return LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL;
				}
			}
			return defaultBehavior;
		}

		public static boolean isLazy(List<Annotation> annotations) {
			return isLazy(annotations, false);
		}

		/**
		 * Returns a flag which indicate if a field should not be serialized
		 * automatically.
		 *
		 * @param annotations  attribute annotations
		 * @param defaultValue default value if it cannot be determined
		 * @return is lazy
		 */
		public static boolean isLazy(Collection<Annotation> annotations, boolean defaultValue) {
			JsonApiRelation jsonApiRelation = null;
			JsonApiIncludeByDefault includeByDefaultAnnotation = null;
			JsonApiToMany toManyAnnotation = null;
			JsonApiToOne toOneAnnotation = null;
			for (Annotation annotation : annotations) {

				if (annotation instanceof JsonApiRelation) {
					jsonApiRelation = (JsonApiRelation) annotation;
					break;
				}
				if (annotation.annotationType().equals(JsonApiIncludeByDefault.class)) {
					includeByDefaultAnnotation = (JsonApiIncludeByDefault) annotation;
				}
				if (annotation.annotationType().equals(JsonApiToMany.class)) {
					toManyAnnotation = (JsonApiToMany) annotation;
				}
				if (annotation.annotationType().equals(JsonApiToOne.class)) {
					toOneAnnotation = (JsonApiToOne) annotation;
				}
			}
			if (jsonApiRelation != null) {
				switch (jsonApiRelation.serialize()) {
					case LAZY:
						return true;
					case ONLY_ID:
						return false;
					case EAGER:
						return false;
					default:
						throw new UnsupportedOperationException("Unknown serialize type " + jsonApiRelation.serialize());
				}
			} else if (includeByDefaultAnnotation != null) {
				return false;
			} else if (toManyAnnotation != null) {
				return toManyAnnotation.lazy();
			} else if (toOneAnnotation != null) {
				return toOneAnnotation.lazy();
			}
			return defaultValue;
		}

		public static ResourceFieldType getResourceFieldType(List<Annotation> annotations) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof JsonApiId) {
					return ResourceFieldType.ID;
				} else if (annotation instanceof JsonApiToOne || annotation instanceof JsonApiToMany || annotation instanceof JsonApiRelation) {
					return ResourceFieldType.RELATIONSHIP;
				} else if (annotation instanceof JsonApiMetaInformation) {
					return ResourceFieldType.META_INFORMATION;
				} else if (annotation instanceof JsonApiLinksInformation) {
					return ResourceFieldType.LINKS_INFORMATION;
				}
			}
			return ResourceFieldType.ATTRIBUTE;
		}

		public List<Annotation> getAnnotations() {
			return annotations;
		}

		public boolean isAnnotationPresent(Class<?> annotationClass) {
			for (Annotation annotation : annotations) {
				if (annotation.annotationType().equals(annotationClass)) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public void init(ResourceInformationBuilderContext context) {
		this.context = context;
	}

}
