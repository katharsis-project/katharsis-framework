package io.katharsis.meta.mock.model;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiLinksInformation;
import io.katharsis.resource.annotations.JsonApiMetaInformation;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.links.LinksInformation;
import io.katharsis.resource.meta.MetaInformation;

@JsonApiResource(type = "tasks")
public class Task {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiToOne(opposite = "tasks")
	private Schedule schedule;

	@JsonApiMetaInformation
	private TaskMetaInformation metaInformation;

	@JsonApiLinksInformation
	private TaskLinksInformation linksInformation;

	public static class TaskMetaInformation implements MetaInformation {

		public String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class TaskLinksInformation implements LinksInformation {

		public String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public Long getId() {
		return id;
	}

	public Task setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

}
