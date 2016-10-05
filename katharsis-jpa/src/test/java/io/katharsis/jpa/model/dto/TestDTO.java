package io.katharsis.jpa.model.dto;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "testDTO")
public class TestDTO {

	public static final String ATTR_COMPUTED_UPPER_STRING_VALUE = "computedUpperStringValue";

	public static String ATTR_COMPUTED_NUMBER_OF_SMALLER_IDS = "computedNumberOfSmallerIds";

	@JsonApiId
	private Long id;

	private String stringValue;

	private String computedUpperStringValue;

	private long computedNumberOfSmallerIds;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public String getComputedUpperStringValue() {
		return computedUpperStringValue;
	}

	public void setComputedUpperStringValue(String computedUpperStringValue) {
		this.computedUpperStringValue = computedUpperStringValue;
	}

	public long getComputedNumberOfSmallerIds() {
		return computedNumberOfSmallerIds;
	}

	public void setComputedNumberOfSmallerIds(long computedNumberOfSmallerIds) {
		this.computedNumberOfSmallerIds = computedNumberOfSmallerIds;
	}
}
