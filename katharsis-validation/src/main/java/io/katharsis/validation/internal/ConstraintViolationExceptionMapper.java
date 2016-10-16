package io.katharsis.validation.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path.Node;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.errorhandling.ErrorDataBuilder;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.ExceptionMapper;
import io.katharsis.module.Module.ModuleContext;
import io.katharsis.resource.exception.init.ResourceNotFoundInitializationException;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.PreconditionUtil;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.StringUtils;

public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

	private static final String HIBERNATE_PROPERTY_NODE_IMPL = "org.hibernate.validator.path.PropertyNode";

	protected static final String META_RESOURCE_ID = "resourceId";
	protected static final String META_RESOURCE_TYPE = "resourceType";
	protected static final String META_RESOURCE_PATH = "attributePath";

	private static final String META_TYPE_KEY = "type";
	private static final Object META_TYPE_VALUE = "ConstraintViolation";

	private ModuleContext context;
	
	private static final int UNPROCESSABLE_ENTITY_422 = 422;

	public ConstraintViolationExceptionMapper(ModuleContext context) {
		this.context = context;
	}

	@Override
	public ErrorResponse toErrorResponse(ConstraintViolationException cve) {
		List<ErrorData> errors = new ArrayList<>();
		for (ConstraintViolation<?> violation : cve.getConstraintViolations()) {

			ErrorDataBuilder builder = ErrorData.builder();
			builder = builder.addMetaField(META_TYPE_KEY, META_TYPE_VALUE);
			builder = builder.setStatus(String.valueOf(UNPROCESSABLE_ENTITY_422));
			builder = builder.setTitle(violation.getMessage());
			builder = builder.setDetail(violation.getMessage());

			builder = builder.setCode(violation.getMessageTemplate());

			if (violation.getRootBean() != null) {
				ResourceRef resourceRef = resolveLeafResourcePath(violation);
				builder = builder.addMetaField(META_RESOURCE_ID, resourceRef.getResourceId());
				builder = builder.addMetaField(META_RESOURCE_TYPE, resourceRef.getResourceType());
				builder = builder.addMetaField(META_RESOURCE_PATH, resourceRef.getPathString());

				if (violation.getRootBean() == violation.getLeafBean()) {
					builder = builder.setSourcePointer(createSourcePointer(resourceRef));
				}
				else {
					// json pointer syntax quite limited, we would need to
					// reference the array position within includes as a source
					// pointer needs access to request for this
				}
			}

			ErrorData error = builder.build();
			errors.add(error);
		}

		return ErrorResponse.builder().setStatus(UNPROCESSABLE_ENTITY_422).setErrorData(errors).build();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ConstraintViolationException fromErrorResponse(ErrorResponse errorResponse) {
		Set violations = new HashSet();

		Iterable<ErrorData> errors = errorResponse.getErrors();
		for (ErrorData error : errors) {
			ConstraintViolationImpl violation = ConstraintViolationImpl.fromError(context.getResourceRegistry(), error);
			violations.add(violation);
		}

		return new ConstraintViolationException(null, violations);
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		if (errorResponse.getHttpStatus() != UNPROCESSABLE_ENTITY_422) {
			return false;
		}
		Iterator<ErrorData> errors = errorResponse.getErrors().iterator();
		if (!errors.hasNext()) {
			return false;
		}
		ErrorData error = errors.next();
		Map<String, Object> meta = error.getMeta();
		return meta != null && META_TYPE_VALUE.equals(meta.get(META_TYPE_KEY));
	}

	/**
	 * Translate validated bean and root path into validated resource and
	 * resource path. For example, embeddables belonging to an entity resource
	 * are mapped back to an entity violation and a proper path to the
	 * embeddable attribute.
	 */
	protected ResourceRef resolveLeafResourcePath(ConstraintViolation<?> violation) {
		Object resource = violation.getRootBean();
		assertResource(resource);

		Object nodeObject = resource;
		ResourceRef ref = new ResourceRef(resource);

		Iterator<Node> iterator = violation.getPropertyPath().iterator();
		while (iterator.hasNext()) {
			Node node = iterator.next();

			// visit list, set, map references
			nodeObject = getNodeReference(nodeObject, node);
			ref.visitNode(nodeObject);

			// visit property
			nodeObject = ref.visitProperty(nodeObject, node);
		}
		return ref;
	}

	private void assertResource(Object resource) {
		if (!isResource(resource.getClass())) {
			throw new IllegalStateException("a resource must be used as root, got " + resource + " instead");
		}
	}

	private static Object getNodeReference(Object element, Node node) {
		Integer index = node.getIndex();
		Object key = node.getKey();
		if (index != null) {
			return ((List<?>) element).get(index);
		}
		else if (key != null) {
			return ((Map<?, ?>) element).get(key);
		}
		else if (element instanceof Set && getValue(node) != null) {
			return getValue(node);
		}
		return element;
	}

	private static Object getValue(Node propertyNode) {
		// bean validation not sufficient for sets
		// not possible to access elements, reverting to
		// Hibernate implementation
		// TODO investigate other implementation next to
		// hibernate, JSR 303 v1.1 not sufficient
		if (propertyNode.getClass().getName().equals(HIBERNATE_PROPERTY_NODE_IMPL)) { // NOSONAR
			try {
				Method valueMethod = propertyNode.getClass().getMethod("getValue");
				return valueMethod.invoke(propertyNode);
			}
			catch (Exception e) {
				throw new UnsupportedOperationException(e);
			}
		}
		else {
			throw new UnsupportedOperationException("cannot convert violations for java.util.Set elements, consider using Hibernate validator");
		}
	}

	class ResourceRef {

		private Object resource;
		private List<String> path = new ArrayList<>();

		public ResourceRef(Object resource) {
			this.resource = resource;
		}

		public String getPathString() {
			return StringUtils.join(".", path);
		}

		public Object visitProperty(Object nodeObject, Node node) {
			if (node.getKind() == ElementKind.PROPERTY) {
				path.add(node.getName());
				return PropertyUtils.getProperty(nodeObject, node.getName());
			}
			else if (node.getKind() == ElementKind.BEAN) {
				return nodeObject;
			}
			else {
				throw new UnsupportedOperationException("unknown node: " + node);
			}
		}

		public void visitNode(Object nodeValue) {
			boolean isResource = nodeValue != null && isResource(nodeValue.getClass());
			if (isResource) {
				path.clear();
				resource = nodeValue;
			}
		}

		public Object getResourceId() {
			return ConstraintViolationExceptionMapper.this.getResourceId(resource);
		}

		public Object getResourceType() {
			return ConstraintViolationExceptionMapper.this.getResourceType(resource);
		}

		/**
		 * Leaf resource being validated.
		 */
		public Object getResource() {
			return resource;
		}

		/**
		 * Path within the leaf resource being validated.
		 */
		public List<String> getPath() {
			return path;
		}

		public boolean isAssociation() {
			if (path.size() != 1) {
				return false;
			}
			ResourceRegistry resourceRegistry = context.getResourceRegistry();
			RegistryEntry<?> entry = resourceRegistry.getEntry(resource.getClass());
			ResourceInformation resourceInformation = entry.getResourceInformation();
			ResourceField relationshipField = resourceInformation.findRelationshipFieldByName(path.get(0));
			return relationshipField != null;
		}
	}

	private boolean isResource(Class<?> clazz) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		try {
			// TODO better API
			resourceRegistry.getEntry(clazz);
			return true;
		} catch (ResourceNotFoundInitializationException e) { // NOSONAR
			return false;
		}
	}

	private String createSourcePointer(ResourceRef ref) {
		String attrPath = ref.toString().replaceAll(".", "/");
		if (ref.path.isEmpty()) {
			return "/data";
		}
		else if (ref.isAssociation()) {
			PreconditionUtil.assertEquals("relations must be added to the resource directly", 1, ref.path.size());
			return "/data/attributes/" + attrPath;
		}
		else {
			// TODO in case of collection an array access is needed
			return "/data/relationships/" + attrPath;
		}
	}

	/**
	 * @return id of the given resource
	 */
	protected String getResourceId(Object resource) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		RegistryEntry<?> entry = resourceRegistry.getEntry(resource.getClass());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		ResourceField idField = resourceInformation.getIdField();
		Object id = PropertyUtils.getProperty(resource, idField.getUnderlyingName());
		if (id != null) {
			return id.toString();
		}
		return null;
	}

	protected String getResourceType(Object resource) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		RegistryEntry<?> entry = resourceRegistry.getEntry(resource.getClass());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		return resourceInformation.getResourceType();
	}
}
