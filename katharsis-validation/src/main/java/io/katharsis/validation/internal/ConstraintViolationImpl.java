package io.katharsis.validation.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.metadata.ConstraintDescriptor;

import io.katharsis.errorhandling.ErrorData;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;

// TODO remo: take care of UnsupportedOperationExceptions to adhere to spec
class ConstraintViolationImpl implements ConstraintViolation<Object> {

	private ErrorData errorData;
	private Class<?> resourceClass;
	private Serializable resourceId;
	private Path path;

	private ConstraintViolationImpl(ResourceRegistry resourceRegistry, ErrorData errorData) {
		this.errorData = errorData;

		Map<String, Object> meta = this.errorData.getMeta();
		if (meta != null) {
			String strResourceId = (String) meta.get(ConstraintViolationExceptionMapper.META_RESOURCE_ID);
			String resourceType = (String) meta.get(ConstraintViolationExceptionMapper.META_RESOURCE_TYPE);
			String resourcePath = (String) meta.get(ConstraintViolationExceptionMapper.META_RESOURCE_PATH);

			if (resourceType != null) {
				RegistryEntry<?> entry = resourceRegistry.getEntry(resourceType);
				resourceClass = entry.getResourceInformation().getResourceClass();
				resourceId = entry.getResourceInformation().parseIdString(strResourceId);
			}
			if (resourcePath != null) {
				path = new PathImpl(resourcePath);
			}
		}
	}

	public static ConstraintViolationImpl fromError(ResourceRegistry resourceRegistry, ErrorData error) {
		return new ConstraintViolationImpl(resourceRegistry, error);
	}
	
	public Serializable getResourceId(){
		return resourceId;
	}

	class PathImpl implements Path {

		private List<Node> nodes;

		public PathImpl(String path) {
			String[] strPathElements = path.split("\\.");

			nodes = new ArrayList<>();
			for (String strPathElement : strPathElements) {
				nodes.add(new NodeImpl(strPathElement));
			}
		}

		@Override
		public Iterator<Node> iterator() {
			return nodes.iterator();
		}
	}

	class NodeImpl implements Node {

		private String name;

		public NodeImpl(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isInIterable() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Integer getIndex() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getKey() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ElementKind getKind() {
			if (path == null) {
				return ElementKind.BEAN;
			}
			else {
				return ElementKind.PROPERTY;
			}
		}

		@Override
		public <T extends Node> T as(Class<T> nodeType) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Object getRootBean() {
		return null;
	}

	@Override
	public Object getLeafBean() {
		return null;
	}

	@Override
	public Object getInvalidValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] getExecutableParameters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getExecutableReturnValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMessage() {
		return errorData.getDetail();
	}

	@Override
	public ConstraintDescriptor<?> getConstraintDescriptor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMessageTemplate() {
		return errorData.getCode();
	}

	@Override
	public Path getPropertyPath() {
		return path;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getRootBeanClass() {
		return resourceClass;
	}

	@Override
	public <U> U unwrap(Class<U> arg0) {
		return null;
	}

}