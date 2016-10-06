package io.katharsis.jpa.model.dto;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToOne;

@JsonApiResource(type = "testDTO")
public class TestDTO {

	public static final String ATTR_COMPUTED_UPPER_STRING_VALUE = "computedUpperStringValue";

	public static String ATTR_COMPUTED_NUMBER_OF_SMALLER_IDS = "computedNumberOfSmallerIds";

	@JsonApiId
	private Long id;

	private String stringValue;

	private String computedUpperStringValue;

	private long computedNumberOfSmallerIds;

	@JsonApiToOne
	@JsonApiLookupIncludeAutomatically
	private RelatedDTO oneRelatedValue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RelatedDTO getOneRelatedValue() {
		return oneRelatedValue;
	}

	public void setOneRelatedValue(RelatedDTO oneRelatedValue) {
		this.oneRelatedValue = oneRelatedValue;
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
