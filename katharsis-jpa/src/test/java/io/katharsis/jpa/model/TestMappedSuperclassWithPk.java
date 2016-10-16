package io.katharsis.jpa.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class TestMappedSuperclassWithPk {

	@Column
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String stringValue) {
		this.id = stringValue;
	}
}
