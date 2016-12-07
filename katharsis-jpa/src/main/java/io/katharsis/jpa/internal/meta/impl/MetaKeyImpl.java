package io.katharsis.jpa.internal.meta.impl;

import java.util.List;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEmbeddable;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.utils.ClassUtils;
import io.katharsis.utils.PreconditionUtil;

public class MetaKeyImpl extends MetaElementImpl implements MetaKey {

	private static final String ID_ELEMENT_SEPARATOR = "-";

	private String name;

	private List<MetaAttribute> elements;

	private boolean unique;

	private MetaType type;

	public MetaKeyImpl(MetaDataObjectImpl parent, String name, List<MetaAttribute> elements, boolean unique, MetaType type) {
		super(parent);
		this.name = name;
		this.elements = elements;
		this.unique = unique;
		this.type = type;
	}

	@Override
	public String getId() {
		return getParent().getId() + "." + getName();
	}

	@Override
	public List<MetaAttribute> getElements() {
		return elements;
	}

	@Override
	public boolean isUnique() {
		return unique;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public MetaType getType() {
		return type;
	}

	@Override
	public MetaAttribute getUniqueElement() {
		if (elements.size() != 1)
			throw new IllegalStateException(getName() + " must contain a single primary key attribute");
		return elements.get(0);
	}

	@Override
	public Object fromKeyString(String idString) {

		// => support compound keys with unique ids
		if (elements.size() == 1) {
			MetaAttribute keyAttr = elements.get(0);
			MetaType keyType = keyAttr.getType();

			if (keyType instanceof MetaEmbeddable) {
				return parseEmbeddableString((MetaEmbeddable) keyType, idString);
			}
			else {
				return keyType.fromString(idString);
			}
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	private Object parseEmbeddableString(MetaEmbeddable embType, String idString) {
		String[] keyElements = idString.split(ID_ELEMENT_SEPARATOR);

		Object id = ClassUtils.newInstance(embType.getImplementationClass());

		List<? extends MetaAttribute> embAttrs = embType.getAttributes();
		if (keyElements.length != embAttrs.size()) {
			throw new UnsupportedOperationException(
					"failed to parse " + idString + ", expected " + elements.size() + " elements");
		}
		for (int i = 0; i < keyElements.length; i++) {
			MetaAttribute embAttr = embAttrs.get(i);
			Object idElement = embAttr.getType().fromString(keyElements[i]);
			embAttr.setValue(id, idElement);
		}
		return id;
	}

	@Override
	public String toKeyString(Object id) {
		// => support compound keys with unique ids
		PreconditionUtil.assertEquals("compound primary key not supported", 1, elements.size());
		MetaAttribute keyAttr = elements.get(0);
		MetaType keyType = keyAttr.getType();
		if (keyType instanceof MetaEmbeddable) {
			MetaEmbeddable embType = (MetaEmbeddable) keyType;
			return toEmbeddableKeyString(embType, id);
		}
		else {
			return id.toString();
		}
	}

	private static String toEmbeddableKeyString(MetaEmbeddable embType, Object id) {
		StringBuilder builder = new StringBuilder();
		List<? extends MetaAttribute> embAttrs = embType.getAttributes();
		for (int i = 0; i < embAttrs.size(); i++) {
			MetaAttribute embAttr = embAttrs.get(i);
			Object idElement = embAttr.getValue(id);
			if (i > 0) {
				builder.append(ID_ELEMENT_SEPARATOR);
			}
			builder.append(idElement);
		}
		return builder.toString();
	}

}
