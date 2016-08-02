package io.katharsis.jpa.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import io.katharsis.jpa.internal.query.AnyTypeObject;

@Embeddable
public class TestAnyType implements AnyTypeObject {

	@Column
	private String type;

	@Column
	private String stringValue;

	@Column
	private Integer intValue;

	@Override
	public String getType() {
		return type;
	}

	@Override
	public Object getValue() {
		if ("intValue".equals(type))
			return intValue;
		else if ("stringValue".equals(type))
			return stringValue;
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Class<T> clazz) {
		return (T) getValue();
	}

	@Override
	public void setValue(Object value) {
		if (value == null) {
			intValue = null;
			stringValue = null;
			type = null;
		} else if (value instanceof String) {
			intValue = null;
			stringValue = (String) value;
			type = "stringValue";
		} else {
			intValue = (Integer) value;
			stringValue = null;
			type = "intValue";
		}
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

	public void setType(String type) {
		this.type = type;
	}

}
