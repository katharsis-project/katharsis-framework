package io.katharsis.jpa.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class TestEntity {

	public static final String ATTR_id = "id";

	public static final String ATTR_stringValue = "stringValue";

	public static final String ATTR_longValue = "longValue";

	public static final String ATTR_mapValue = "mapValue";
	public static final String ATTR_oneRelatedValue = "oneRelatedValue";
	public static final String ATTR_eagerRelatedValue = "eagerOneRelatedValue";
	public static final String ATTR_manyRelatedValues = "manyRelatedValues";

	public static final String ATTR_embValue = "embValue";
	public static final String ATTR_embValue_intValue = TestEntity.ATTR_embValue + "."
			+ TestEmbeddable.ATTR_embIntValue;
	public static final String ATTR_embValue_stringValue = TestEntity.ATTR_embValue + "."
			+ TestEmbeddable.ATTR_embStringValue;
	public static final String ATTR_embValue_anyValue = TestEntity.ATTR_embValue + "." + TestEmbeddable.ATTR_anyValue;
	public static final String ATTR_embValue_nestedValue_boolValue = TestEntity.ATTR_embValue + "."
			+ TestEmbeddable.ATTR_nestedValue + "." + TestNestedEmbeddable.ATTR_embBoolValue;

	@Id
	private Long id;

	@Column
	private String stringValue;

	@Column
	private long longValue;

	@Column
	private TestEmbeddable embValue;

	@ElementCollection(fetch = FetchType.EAGER)
	private Map<String, String> mapValue;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private RelatedEntity oneRelatedValue;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn
	private RelatedEntity eagerRelatedValue;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "testEntity")
	private List<RelatedEntity> manyRelatedValues;

	public TestEntity() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Map<String, String> getMapValue() {
		if (mapValue == null)
			mapValue = new HashMap<String, String>();
		return mapValue;
	}

	public void setMapValue(Map<String, String> mapValue) {
		this.mapValue = mapValue;
	}

	public RelatedEntity getOneRelatedValue() {
		return oneRelatedValue;
	}

	public void setOneRelatedValue(RelatedEntity oneRelatedValue) {
		this.oneRelatedValue = oneRelatedValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	
	
	public RelatedEntity getEagerRelatedValue() {
		return eagerRelatedValue;
	}

	public void setEagerRelatedValue(RelatedEntity eagerRelatedValue) {
		this.eagerRelatedValue = eagerRelatedValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	public TestEmbeddable getEmbValue() {
		return embValue;
	}

	public void setEmbValue(TestEmbeddable embValue) {
		this.embValue = embValue;
	}

	public List<RelatedEntity> getManyRelatedValues() {
		if (manyRelatedValues == null)
			manyRelatedValues = new ArrayList<RelatedEntity>();
		return manyRelatedValues;
	}

	public void setManyRelatedValues(List<RelatedEntity> manyRelatedValues) {
		this.manyRelatedValues = manyRelatedValues;
	}
}
