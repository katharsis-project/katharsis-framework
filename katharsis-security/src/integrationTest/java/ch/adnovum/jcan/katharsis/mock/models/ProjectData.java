package ch.adnovum.jcan.katharsis.mock.models;

import javax.validation.constraints.NotNull;

public class ProjectData {

	@NotNull
	private String value;

	public String getValue() {
		return value;
	}

	public ProjectData setValue(@SuppressWarnings("SameParameterValue") String value) {
		this.value = value;
		return this;
	}
}
