package io.katharsis.jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TestIdEmbeddable implements Serializable {

	private static final long serialVersionUID = 4473954915317129238L;

	public static final String ATTR_embIntValue = "embIntValue";
	public static final String ATTR_embStringValue = "embStringValue";

	@Column
	private Integer embIntValue;

	@Column
	private String embStringValue;

	public TestIdEmbeddable() {
	}

	public TestIdEmbeddable(Integer intValue, String stringValue) {
		this.embIntValue = intValue;
		this.embStringValue = stringValue;
	}

	public Integer getEmbIntValue() {
		return embIntValue;
	}

	public void setEmbIntValue(Integer embIntValue) {
		this.embIntValue = embIntValue;
	}

	public String getEmbStringValue() {
		return embStringValue;
	}

	public void setEmbStringValue(String embStringValue) {
		this.embStringValue = embStringValue;
	}
}
